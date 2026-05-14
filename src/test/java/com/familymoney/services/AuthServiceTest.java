package com.familymoney.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.familymoney.domains.auth.exceptions.EmailAlreadyVerifiedException;
import com.familymoney.domains.auth.exceptions.EmailNotFoundException;
import com.familymoney.domains.auth.exceptions.NewEmailVerificationTooSoonException;
import com.familymoney.domains.auth.exceptions.RefreshTokenInvalidException;
import com.familymoney.domains.auth.exceptions.RefreshTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.UserAlreadyExistsException;
import com.familymoney.domains.auth.exceptions.VerificationTokenExpiredException;
import com.familymoney.domains.auth.exceptions.VerificationTokenNotFoundException;
import com.familymoney.domains.auth.repositories.IEmailVerificationRepository;
import com.familymoney.domains.auth.repositories.IRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.ITokenFamilyBlacklistRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.services.AuthService;
import com.familymoney.domains.auth.services.IEmailSenderService;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.repositories.IRoleRepository;
import com.familymoney.domains.user.repositories.IUserRepository;
import com.familymoney.domains.user.repositories.dtos.CreateUserDto;
import com.familymoney.domains.user.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.user.repositories.entitites.UserEntity;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Password;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.security.JwtUtils;
import com.familymoney.security.UserPasswordEncoder;
import com.familymoney.utils.FakeGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthServiceTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private IUserRepository userRepository;
  @Mock private IRoleRepository permissionsRepository;
  @Mock private IEmailSenderService emailSenderService;
  @Spy private UserPasswordEncoder passwordEncoder;
  @Spy private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
  @Mock private IEmailVerificationRepository emailVerificationRepository;
  @Mock private ITokenFamilyBlacklistRepository tokenFamilyBlacklistRepository;
  @Mock private JwtUtils jwtUtils;
  @Mock private IRefreshTokenRepository refreshTokenRepository;

  @InjectMocks private AuthService authService;

  private void mockUserRepositoryCreate() {
    lenient()
        .when(userRepository.create(any(CreateUserDto.class)))
        .thenAnswer(
            invocation -> {
              CreateUserDto dto = invocation.getArgument(0, CreateUserDto.class);
              return Optional.of(
                  UserEntity.builder()
                      .id(dto.id()) // return the same UserId received
                      .username(dto.username())
                      .email(dto.email())
                      .hashedPassword(
                          dto.passwordHash()) // use dto.passwordHash() or provide a test value
                      .createdAt(now)
                      .updatedAt(now)
                      .isEmailVerified(false)
                      .isEnabled(true)
                      .build());
            });
  }

  private void mockEmailVerificationRepositoryCreate() {
    lenient()
        .when(emailVerificationRepository.create(any(CreateEmailVerificationDto.class)))
        .thenAnswer(
            invocation -> {
              CreateEmailVerificationDto dto =
                  invocation.getArgument(0, CreateEmailVerificationDto.class);
              return Optional.of(
                  EmailVerificationEntity.builder()
                      .userId(dto.userId())
                      .token(dto.token())
                      .createdAt(now)
                      .updatedAt(now)
                      .expiresAt(dto.expiresAt())
                      .build());
            });
  }

  private void mockRefreshTokenRepositoryCreate() {
    lenient()
        .when(refreshTokenRepository.create(any(CreateRefreshTokenDto.class)))
        .thenAnswer(
            invocation -> {
              CreateRefreshTokenDto dto = invocation.getArgument(0, CreateRefreshTokenDto.class);
              return Optional.of(
                  RefreshTokenEntity.builder()
                      .id(dto.id())
                      .userId(dto.userId())
                      .token(dto.token())
                      .createdAt(now)
                      .updatedAt(now)
                      .expiresAt(dto.expiresAt())
                      .family(dto.family())
                      .build());
            });
  }

  private void mockUserRepositoryFindByEmail(
      final boolean isEnabled, final boolean isEmailVerified) {
    when(userRepository.findByEmail(any(Email.class)))
        .thenAnswer(
            invocation -> {
              Email email = invocation.getArgument(0, Email.class);
              return Optional.of(
                  UserEntity.builder()
                      .id(UserId.generate())
                      .username(UserName.fromString(FakeGenerator.username()))
                      .email(email)
                      .hashedPassword("hashed-password")
                      .createdAt(now)
                      .updatedAt(now)
                      .isEmailVerified(isEmailVerified)
                      .isEnabled(isEnabled)
                      .build());
            });
  }

  private void mockRefreshTokenFindByToken() {
    when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
        .thenAnswer(
            invocation -> {
              RefreshToken refreshToken = invocation.getArgument(0, RefreshToken.class);
              return Optional.of(
                  RefreshTokenEntity.builder()
                      .id(UUID.randomUUID())
                      .userId(UserId.generate())
                      .token(refreshToken)
                      .createdAt(now)
                      .updatedAt(now)
                      .expiresAt(now.plusSeconds(3600))
                      .family(TokenFamily.generate())
                      .build());
            });
  }

  private void mockEmailVerificationTokenFindByToken() {
    when(emailVerificationRepository.findByToken(any(EmailVerificationToken.class)))
        .thenAnswer(
            invocation -> {
              EmailVerificationToken emailVerificationToken =
                  invocation.getArgument(0, EmailVerificationToken.class);
              return Optional.of(
                  EmailVerificationEntity.builder()
                      .userId(UserId.generate())
                      .token(emailVerificationToken)
                      .createdAt(now)
                      .updatedAt(now)
                      .expiresAt(now.plusSeconds(3600))
                      .build());
            });
  }

  // region AuthService.registerUser()

  @Test
  void registerUser_succeeds_when_register_user_for_the_first_time() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(false);
    mockUserRepositoryCreate();
    mockEmailVerificationRepositoryCreate();

    assertDoesNotThrow(() -> authService.registerUser(username, email, password));

    verify(userRepository).create(any(CreateUserDto.class));
    verify(emailVerificationRepository).create(any(CreateEmailVerificationDto.class));
    verify(permissionsRepository).setRoleForUserId(any(UserId.class), any(Role.class));
  }

  @Test
  void registerUser_throws_when_user_already_exists() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class,
        () -> authService.registerUser(username, email, password));
    verify(userRepository, never()).create(any(CreateUserDto.class));
  }

  @Test
  void registerUser_throws_when_user_table_fails() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(false);
    when(userRepository.create(any(CreateUserDto.class))).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () -> authService.registerUser(username, email, password));
  }

  @Test
  void registerUser_fails_when_email_verification_table() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.existsByEmailOrUsername(email, username)).thenReturn(false);
    mockUserRepositoryCreate();
    when(emailVerificationRepository.create(any(CreateEmailVerificationDto.class)))
        .thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () -> authService.registerUser(username, email, password));
  }

  // endregion

  // region AuthService.loginUser()

  @Test
  void loginUser_returns_tokens_when_called_with_correct_credentials() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    mockUserRepositoryFindByEmail(true, true);
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
        .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));
    mockRefreshTokenRepositoryCreate();

    assertDoesNotThrow(
        () -> {
          val tokens = authService.loginUser(email, password);
          assertNotNull(tokens);
          assertNotNull(tokens.accessToken());
          assertNotNull(tokens.refreshToken());
        });

    verify(refreshTokenRepository).create(any(CreateRefreshTokenDto.class));
  }

  @Test
  void loginUser_throws_when_called_with_incorrect_password() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    mockUserRepositoryFindByEmail(true, true);
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void loginUser_throws_when_no_user_with_that_email_exists() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void loginUser_throws_when_refresh_token_table_fails() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    mockUserRepositoryFindByEmail(true, true);
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
        .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));
    when(refreshTokenRepository.create(any(CreateRefreshTokenDto.class)))
        .thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void loginUser_throws_when_user_has_not_verified_email() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    mockUserRepositoryFindByEmail(true, false);

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void loginUser_throws_when_user_is_not_enabled() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    mockUserRepositoryFindByEmail(false, true);

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  // endregion

  // region AuthService.refreshTokens()

  @Test
  void refreshTokens_with_valid_refresh_token_succeeds() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    mockRefreshTokenFindByToken();
    when(refreshTokenRepository.updateByToken(any(), any())).thenReturn(true);
    when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
        .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));
    mockRefreshTokenRepositoryCreate();

    assertDoesNotThrow(
        () -> {
          val tokens = authService.refreshTokens(refreshToken);
          assertNotNull(tokens);
          assertNotNull(tokens.accessToken());
          assertNotNull(tokens.refreshToken());
        });

    verify(refreshTokenRepository).updateByToken(any(), any());
    verify(refreshTokenRepository).create(any());
  }

  @Test
  void refreshTokens_throws_when_refresh_token_doesnt_exist() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    when(refreshTokenRepository.findByToken(any(RefreshToken.class))).thenReturn(Optional.empty());

    assertThrows(
        RefreshTokenNotFoundException.class, () -> authService.refreshTokens(refreshToken));
  }

  @Test
  void refreshTokens_throws_when_refresh_token_table_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    mockRefreshTokenFindByToken();
    when(refreshTokenRepository.updateByToken(any(), any())).thenReturn(true);
    when(jwtUtils.generateAccessToken(any(UserId.class), any(TokenFamily.class)))
        .thenReturn(AccessToken.fromString(FakeGenerator.accessToken()));
    when(refreshTokenRepository.create(any())).thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.refreshTokens(refreshToken));
  }

  @Test
  void refreshTokens_throws_when_refresh_token_is_expired() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
        .thenAnswer(
            invocation -> {
              RefreshToken rf = invocation.getArgument(0, RefreshToken.class);
              return Optional.of(
                  RefreshTokenEntity.builder()
                      .id(UUID.randomUUID())
                      .userId(UserId.generate())
                      .token(rf)
                      .createdAt(now.minusSeconds(3600))
                      .expiresAt(now.minusSeconds(10))
                      .family(TokenFamily.generate())
                      .build());
            });

    assertThrows(RefreshTokenInvalidException.class, () -> authService.refreshTokens(refreshToken));
  }

  // endregion

  // region AuthService.verifyEmail()

  @Test
  void verifyEmail_succeeds_when_called_with_valid_email_verification_token() {
    val emailVerificationToken =
        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

    mockEmailVerificationTokenFindByToken();
    when(userRepository.updateById(any(UserId.class), any(UpdateUserDto.class))).thenReturn(true);
    when(emailVerificationRepository.deleteByUserId(any(UserId.class))).thenReturn(true);

    assertDoesNotThrow(() -> authService.verifyEmail(emailVerificationToken));

    verify(userRepository).updateById(any(UserId.class), any(UpdateUserDto.class));
    verify(emailVerificationRepository).deleteByUserId(any(UserId.class));
  }

  @Test
  void verifyEmail_throws_when_email_verification_token_doesnt_exist() {
    val emailVerificationToken =
        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

    when(emailVerificationRepository.findByToken(emailVerificationToken))
        .thenReturn(Optional.empty());

    assertThrows(
        VerificationTokenNotFoundException.class,
        () -> authService.verifyEmail(emailVerificationToken));
  }

  @Test
  void verifyEmail_throws_when_email_verification_token_is_expired() {
    val emailVerificationToken =
        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

    when(emailVerificationRepository.findByToken(any(EmailVerificationToken.class)))
        .thenAnswer(
            invocation -> {
              EmailVerificationToken evt = invocation.getArgument(0, EmailVerificationToken.class);
              return Optional.of(
                  EmailVerificationEntity.builder()
                      .userId(UserId.generate())
                      .token(evt)
                      .createdAt(now.minusSeconds(3600))
                      .updatedAt(now.minusSeconds(3600))
                      .expiresAt(now.minusSeconds(1))
                      .build());
            });

    assertThrows(
        VerificationTokenExpiredException.class,
        () -> authService.verifyEmail(emailVerificationToken));

    verify(userRepository, never()).updateById(any(UserId.class), any(UpdateUserDto.class));
    verify(emailVerificationRepository, never()).deleteByUserId(any(UserId.class));
  }

  @Test
  void verifyEmail_throws_when_updating_user_fails() {
    val emailVerificationToken =
        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

    mockEmailVerificationTokenFindByToken();
    when(userRepository.updateById(any(UserId.class), any(UpdateUserDto.class))).thenReturn(false);

    assertThrows(
        DatabaseExecutionException.class, () -> authService.verifyEmail(emailVerificationToken));

    verify(userRepository).updateById(any(UserId.class), any(UpdateUserDto.class));
    verify(emailVerificationRepository, never()).deleteByUserId(any(UserId.class));
  }

  @Test
  void verifyEmail_throws_when_deleting_email_verification_token_fails() {
    val emailVerificationToken =
        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

    mockEmailVerificationTokenFindByToken();
    when(userRepository.updateById(any(UserId.class), any(UpdateUserDto.class))).thenReturn(true);
    when(emailVerificationRepository.deleteByUserId(any(UserId.class))).thenReturn(false);

    assertThrows(
        DatabaseExecutionException.class, () -> authService.verifyEmail(emailVerificationToken));

    verify(userRepository, never()).updateById(any(UserId.class), any(UpdateUserDto.class));
    verify(emailVerificationRepository).deleteByUserId(any(UserId.class));
  }

  // endregion

  // region AuthService.resendVerificationEmail()

  @Test
  void resendVerificationEmail_sends_when_last_attempt_was_long_time_ago() {
    val email = Email.fromString(FakeGenerator.email());

    mockUserRepositoryFindByEmail(true, false);
    when(emailVerificationRepository.findByUserId(any(UserId.class)))
        .thenReturn(
            Optional.of(
                EmailVerificationEntity.builder()
                    .userId(UserId.generate())
                    .token(
                        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken()))
                    .createdAt(now.minusSeconds(3600))
                    .updatedAt(now.minusSeconds(3600))
                    .expiresAt(now.plusSeconds(100))
                    .build()));
    mockEmailVerificationRepositoryCreate();

    assertDoesNotThrow(() -> authService.resendVerificationEmail(email));

    verify(emailVerificationRepository).create(any(CreateEmailVerificationDto.class));
    verify(emailSenderService)
        .sendEmailVerificationEmail(
            any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
  }

  @Test
  void resendVerificationEmail_throws_when_last_attempt_was_recently() {
    val email = Email.fromString(FakeGenerator.email());

    mockUserRepositoryFindByEmail(true, false);
    mockEmailVerificationRepositoryCreate();
    mockEmailVerificationRepositoryCreate();
    when(emailVerificationRepository.updateByUserId(
            any(UserId.class), any(UpdateEmailVerificationTokenDto.class)))
        .thenReturn(true);

    assertThrows(
        NewEmailVerificationTooSoonException.class,
        () -> authService.resendVerificationEmail(email));

    verify(emailVerificationRepository, never())
        .updateByUserId(any(UserId.class), any(UpdateEmailVerificationTokenDto.class));
    verify(emailVerificationRepository, never()).create(any(CreateEmailVerificationDto.class));
    verify(emailSenderService, never())
        .sendEmailVerificationEmail(
            any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
  }

  @Test
  void resendVerificationEmail_throws_when_updating_email_verification_token_fails() {
    val email = Email.fromString(FakeGenerator.email());

    mockUserRepositoryFindByEmail(true, false);
    mockEmailVerificationRepositoryCreate();
    mockEmailVerificationRepositoryCreate();
    when(emailVerificationRepository.updateByUserId(
            any(UserId.class), any(UpdateEmailVerificationTokenDto.class)))
        .thenReturn(false);

    assertThrows(
        NewEmailVerificationTooSoonException.class,
        () -> authService.resendVerificationEmail(email));

    verify(emailVerificationRepository, never()).create(any(CreateEmailVerificationDto.class));
    verify(emailSenderService, never())
        .sendEmailVerificationEmail(
            any(Email.class), any(UserName.class), any(EmailVerificationToken.class));
  }

  @Test
  void resendVerificationEmail_throws_when_no_user_with_that_email_exists() {
    val email = Email.fromString(FakeGenerator.email());
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(EmailNotFoundException.class, () -> authService.resendVerificationEmail(email));
  }

  @Test
  void resendVerificationEmail_throws_when_user_email_is_already_verified() {
    val email = Email.fromString(FakeGenerator.email());
    mockUserRepositoryFindByEmail(true, true);

    assertThrows(
        EmailAlreadyVerifiedException.class, () -> authService.resendVerificationEmail(email));
  }

  // endregion

  // region AuthService.forgotPassword()

  // TODO

  // endregion

  // region AuthService.resetPassword()

  // TODO

  // endregion

  // region AuthService.logoutUser()

  @Test
  void logoutUser_succeeds_when_token_is_valid() {
    val refreshToken = RefreshToken.generate();

    mockRefreshTokenFindByToken();
    when(refreshTokenRepository.deleteByToken(any(RefreshToken.class))).thenReturn(true);
    when(tokenFamilyBlacklistRepository.deleteByFamily(any(TokenFamily.class))).thenReturn(true);

    assertDoesNotThrow(() -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository).deleteByToken(any(RefreshToken.class));
    verify(tokenFamilyBlacklistRepository).deleteByFamily(any(TokenFamily.class));
  }

  @Test
  void logoutUser_throws_when_refresh_token_doesnt_exist() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

    assertThrows(RefreshTokenNotFoundException.class, () -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
    verify(tokenFamilyBlacklistRepository, never()).deleteByFamily(any(TokenFamily.class));
  }

  @Test
  void logoutUser_throws_when_refresh_token_is_expired() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    when(refreshTokenRepository.findByToken(any(RefreshToken.class)))
        .thenAnswer(
            invocation -> {
              RefreshToken rt = invocation.getArgument(0, RefreshToken.class);
              return Optional.of(
                  RefreshTokenEntity.builder()
                      .id(UUID.randomUUID())
                      .userId(UserId.generate())
                      .token(rt)
                      .createdAt(now.minusSeconds(7200))
                      .updatedAt(now.minusSeconds(7200))
                      .expiresAt(now.minusSeconds(1))
                      .family(TokenFamily.generate())
                      .build());
            });

    assertThrows(RefreshTokenInvalidException.class, () -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
    verify(tokenFamilyBlacklistRepository, never()).deleteByFamily(any(TokenFamily.class));
  }

  @Test
  void logoutUser_throws_when_deleting_refresh_token_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    mockRefreshTokenFindByToken();
    when(refreshTokenRepository.deleteByToken(any(RefreshToken.class))).thenReturn(false);

    assertThrows(DatabaseExecutionException.class, () -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
    verify(tokenFamilyBlacklistRepository, never()).deleteByFamily(any(TokenFamily.class));
  }

  @Test
  void logoutUser_throws_when_blacklisting_family_token_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    mockRefreshTokenFindByToken();
    when(refreshTokenRepository.deleteByToken(any(RefreshToken.class))).thenReturn(true);
    when(tokenFamilyBlacklistRepository.deleteByFamily(any(TokenFamily.class))).thenReturn(false);

    assertThrows(DatabaseExecutionException.class, () -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, never()).deleteByToken(any(RefreshToken.class));
    verify(tokenFamilyBlacklistRepository, never()).deleteByFamily(any(TokenFamily.class));
  }

  // endregion

  // region AuthService.isFamilyBlacklisted()

  // TODO

  // endregion
}
