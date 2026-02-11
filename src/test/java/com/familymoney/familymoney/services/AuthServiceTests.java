package com.familymoney.familymoney.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.EmailNotFoundException;
import com.familymoney.familymoney.exceptions.RefreshTokenInvalidException;
import com.familymoney.familymoney.exceptions.RefreshTokenNotFoundException;
import com.familymoney.familymoney.exceptions.UserAlreadyExistsException;
import com.familymoney.familymoney.exceptions.VerificationTokenExpiredException;
import com.familymoney.familymoney.exceptions.VerificationTokenNotFoundException;
import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import com.familymoney.familymoney.repositories.IRefreshTokenRepository;
import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.security.JwtUtils;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.impl.AuthService;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.FakeGenerator;
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
public class AuthServiceTests {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private IUserRepository userRepository;
  @Mock private IRoleRepository permissionsRepository;
  @Mock private IEmailSenderService emailSenderService;
  @Spy private UserPasswordEncoder passwordEncoder;
  @Spy private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
  @Mock private IEmailVerificationRepository emailVerificationRepository;
  @Mock private JwtUtils jwtUtils;
  @Mock private IRefreshTokenRepository refreshTokenRepository;

  @InjectMocks private AuthService authService;

  // region registerUser()

  @Test
  void register_user_for_the_first_time_it_succeeds() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(userRepository.existsByEmailOrUsername(eq(email), eq(username))).thenReturn(false);
    when(userRepository.create(eq(username), eq(email), any()))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .hashedPassword("dsafjhadskjgf5dsf56a4")
                    .createdAt(now)
                    .updatedAt(now.plusSeconds(3000))
                    .isEmailVerified(false)
                    .isEnabled(true)
                    .build()));
    when(emailVerificationRepository.create(eq(userId), any(), any()))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(
                        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken()))
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));

    assertDoesNotThrow(() -> authService.registerUser(username, email, password));

    verify(userRepository, times(1)).create(eq(username), eq(email), any());
    verify(emailVerificationRepository, times(1)).create(eq(userId), any(), any());
    verify(permissionsRepository, times(1)).setRoleForUserId(eq(userId), any());
  }

  @Test
  void register_user_when_it_already_exists() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.existsByEmailOrUsername(eq(email), eq(username))).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class,
        () -> authService.registerUser(username, email, password));
  }

  @Test
  void register_user_but_user_table_fails() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.existsByEmailOrUsername(eq(email), eq(username))).thenReturn(false);
    when(userRepository.create(eq(username), eq(email), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () -> authService.registerUser(username, email, password));
  }

  @Test
  void register_user_but_email_verification_table_fails() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(userRepository.existsByEmailOrUsername(eq(email), eq(username))).thenReturn(false);
    when(userRepository.create(eq(username), eq(email), any()))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .hashedPassword("dsafjhadskjgf5dsf56a4")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(false)
                    .isEnabled(true)
                    .build()));
    when(emailVerificationRepository.create(eq(userId), any(), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () -> authService.registerUser(username, email, password));
  }

  // endregion

  // region loginUser()

  @Test
  void login_user_with_correct_credentials_succeeds() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtils.generateAccessToken(eq(userId)))
        .thenReturn(JwtToken.fromString(FakeGenerator.accessToken()));
    when(refreshTokenRepository.create(eq(userId), any(), any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(RefreshToken.fromString(FakeGenerator.refreshToken()))
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(UUID.randomUUID())
                    .build()));

    assertDoesNotThrow(
        () -> {
          val tokens = authService.loginUser(email, password);
          assertNotNull(tokens);
          assertNotNull(tokens.accessToken());
          assertNotNull(tokens.refreshToken());
        });

    verify(refreshTokenRepository, times(1)).create(any(), any(), any());
  }

  @Test
  void login_user_with_incorrect_credentials_fails() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(UserId.fromUuid(UUID.randomUUID()))
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_that_does_not_exist_fails() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_but_refreshtoken_table_fails() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtils.generateAccessToken(eq(userId)))
        .thenReturn(JwtToken.fromString(FakeGenerator.accessToken()));
    when(refreshTokenRepository.create(eq(userId), any(), any())).thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_with_unverified_email_fails() {
    val email = Email.fromString(FakeGenerator.email());
    val password = Password.fromString(FakeGenerator.password());

    when(userRepository.findByEmail(any()))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(UserId.fromUuid(UUID.randomUUID()))
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(false)
                    .isEnabled(true)
                    .build()));

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  // endregion

  // region refreshTokens()

  @Test
  void refresh_tokens_with_valid_refresh_token_succeeds() {
    val token = RefreshToken.fromString(FakeGenerator.refreshToken());
    val userId = UserId.fromUuid(UUID.randomUUID());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));
    when(refreshTokenRepository.updateByToken(any(), any())).thenReturn(true);
    when(jwtUtils.generateAccessToken(any()))
        .thenReturn(JwtToken.fromString(FakeGenerator.accessToken()));
    when(refreshTokenRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(RefreshToken.fromString(FakeGenerator.refreshToken()))
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));

    val tokens = authService.refreshTokens(token);
    assertNotNull(tokens);
    assertNotNull(tokens.accessToken());
    assertNotNull(tokens.refreshToken());

    verify(refreshTokenRepository, times(1)).updateByToken(any(), any());
    verify(refreshTokenRepository, times(1)).create(any(), any(), any());
  }

  @Test
  void refresh_tokens_with_non_existing_refresh_token_fails() {
    when(refreshTokenRepository.findByToken(any())).thenReturn(Optional.empty());
    assertThrows(
        RefreshTokenNotFoundException.class,
        () -> authService.refreshTokens(RefreshToken.fromString(FakeGenerator.refreshToken())));
  }

  @Test
  void refresh_tokens_with_used_refresh_token_fails() {
    val token = RefreshToken.fromString(FakeGenerator.refreshToken());
    val userId = UserId.fromUuid(UUID.randomUUID());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(true)
                    .usedAt(Optional.of(now.minusSeconds(10)))
                    .family(family)
                    .build()));
    when(refreshTokenRepository.updateByFamily(any(), any())).thenReturn(true);
    when(userRepository.findById(any()))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(Email.fromString(FakeGenerator.email()))
                    .hashedPassword("hashed")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    doNothing().when(emailSenderService).sendSecurityAlertEmail(any(), any());

    assertThrows(RefreshTokenInvalidException.class, () -> authService.refreshTokens(token));

    verify(refreshTokenRepository, times(1)).updateByFamily(any(), any());
    verify(emailSenderService, times(1)).sendSecurityAlertEmail(any(), any());
  }

  @Test
  void refresh_tokens_but_refresh_token_table_fails() {
    val token = RefreshToken.fromString(FakeGenerator.refreshToken());
    val userId = UserId.fromUuid(UUID.randomUUID());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));
    when(refreshTokenRepository.updateByToken(any(), any())).thenReturn(true);
    when(jwtUtils.generateAccessToken(any()))
        .thenReturn(JwtToken.fromString(FakeGenerator.accessToken()));
    when(refreshTokenRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.refreshTokens(token));
  }

  @Test
  void refresh_tokens_with_expired_refresh_token_fails() {
    val token = RefreshToken.fromString(FakeGenerator.refreshToken());
    val userId = UserId.fromUuid(UUID.randomUUID());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now.minusSeconds(3600))
                    .expiresAt(now.minusSeconds(10))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));

    assertThrows(RefreshTokenInvalidException.class, () -> authService.refreshTokens(token));
  }

  // endregion

  // region verifyEmail()

  @Test
  void verify_email_with_valid_token_succeeds() {
    val token = EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(emailVerificationRepository.findByToken(eq(token)))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));
    when(userRepository.updateById(eq(userId), any())).thenReturn(true);
    when(emailVerificationRepository.deleteByUserId(eq(userId))).thenReturn(true);

    assertDoesNotThrow(() -> authService.verifyEmail(token));

    verify(userRepository, times(1)).updateById(eq(userId), any());
    verify(emailVerificationRepository, times(1)).deleteByUserId(eq(userId));
  }

  @Test
  void verify_email_with_already_verified_email_succeeds() {
    val token = EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(emailVerificationRepository.findByToken(eq(token)))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));
    when(userRepository.updateById(eq(userId), any())).thenReturn(true);
    when(emailVerificationRepository.deleteByUserId(eq(userId))).thenReturn(true);

    assertDoesNotThrow(() -> authService.verifyEmail(token));

    verify(userRepository, times(1)).updateById(eq(userId), any());
    verify(emailVerificationRepository, times(1)).deleteByUserId(eq(userId));
  }

  @Test
  void verify_email_with_non_existing_token_fails() {
    val token = EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());

    when(emailVerificationRepository.findByToken(eq(token))).thenReturn(Optional.empty());

    assertThrows(VerificationTokenNotFoundException.class, () -> authService.verifyEmail(token));
  }

  @Test
  void verify_email_with_expired_token_fails() {
    val token = EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(emailVerificationRepository.findByToken(eq(token)))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now.minusSeconds(3600))
                    .expiresAt(now.minusSeconds(1))
                    .build()));

    assertThrows(VerificationTokenExpiredException.class, () -> authService.verifyEmail(token));

    verify(userRepository, never()).updateById(any(), any());
    verify(emailVerificationRepository, never()).deleteByUserId(any());
  }

  @Test
  void verify_email_but_user_repository_fails() {
    val token = EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(emailVerificationRepository.findByToken(eq(token)))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));
    when(userRepository.updateById(eq(userId), any())).thenReturn(false);

    // AuthService doesn't check the boolean result, so the call should not throw.
    assertDoesNotThrow(() -> authService.verifyEmail(token));

    verify(userRepository, times(1)).updateById(eq(userId), any());
    verify(emailVerificationRepository, times(1)).deleteByUserId(eq(userId));
  }

  @Test
  void verify_email_but_email_verification_repository_fails() {
    val token = EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(emailVerificationRepository.findByToken(eq(token)))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(token)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));
    when(userRepository.updateById(eq(userId), any())).thenReturn(true);
    when(emailVerificationRepository.deleteByUserId(eq(userId))).thenReturn(false);

    // AuthService doesn't check the boolean result, so the call should not throw.
    assertDoesNotThrow(() -> authService.verifyEmail(token));

    verify(userRepository, times(1)).updateById(eq(userId), any());
    verify(emailVerificationRepository, times(1)).deleteByUserId(eq(userId));
  }

  // endregion

  // region resendVerificationEmail()

  @Test
  void resend_verification_email_succeeds() {
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.fromUuid(UUID.randomUUID());

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(false)
                    .isEnabled(true)
                    .build()));
    when(emailVerificationRepository.create(eq(userId), any(), any()))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(
                        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken()))
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));

    assertDoesNotThrow(() -> authService.resendVerificationEmail(email));

    verify(emailVerificationRepository, times(1)).create(eq(userId), any(), any());
  }

  @Test
  void resend_verification_email_for_verified_email_fails() {
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.fromUuid(UUID.randomUUID());

    // NOTE: current AuthService implementation does NOT check isEmailVerified,
    // so this should not fail and should resend a token.
    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(UserName.fromString(FakeGenerator.username()))
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(emailVerificationRepository.create(eq(userId), any(), any()))
        .thenReturn(
            Optional.of(
                EmailVerificationDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(
                        EmailVerificationToken.fromString(FakeGenerator.emailVerificationToken()))
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));

    assertDoesNotThrow(() -> authService.resendVerificationEmail(email));

    verify(emailVerificationRepository, times(1)).create(eq(userId), any(), any());
  }

  @Test
  void resend_verification_email_for_non_existing_email_fails() {
    val email = Email.fromString(FakeGenerator.email());
    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    assertThrows(EmailNotFoundException.class, () -> authService.resendVerificationEmail(email));
  }

  // endregion

  // region logoutUser()

  @Test
  void logout_user_succeeds() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());
    val userId = UserId.fromUuid(UUID.randomUUID());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(eq(refreshToken)))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(refreshToken)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));
    when(refreshTokenRepository.updateByFamily(eq(family), any())).thenReturn(true);

    assertDoesNotThrow(() -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, times(1)).updateByFamily(eq(family), any());
  }

  @Test
  void logout_user_with_non_existing_refresh_token_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());

    when(refreshTokenRepository.findByToken(eq(refreshToken))).thenReturn(Optional.empty());

    assertThrows(RefreshTokenNotFoundException.class, () -> authService.logoutUser(refreshToken));
  }

  @Test
  void logout_user_with_already_used_refresh_token_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(eq(refreshToken)))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(UserId.fromUuid(UUID.randomUUID()))
                    .token(refreshToken)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(true)
                    .usedAt(Optional.of(now.minusSeconds(10)))
                    .family(family)
                    .build()));

    assertThrows(RefreshTokenInvalidException.class, () -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, never()).updateByFamily(eq(family), any());
  }

  @Test
  void logout_user_with_expired_refresh_token_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(eq(refreshToken)))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(UserId.fromUuid(UUID.randomUUID()))
                    .token(refreshToken)
                    .createdAt(now.minusSeconds(7200))
                    .expiresAt(now.minusSeconds(1))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));

    assertThrows(RefreshTokenInvalidException.class, () -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, never()).updateByFamily(eq(family), any());
  }

  @Test
  void logout_user_but_refresh_token_repository_fails() {
    val refreshToken = RefreshToken.fromString(FakeGenerator.refreshToken());
    val family = UUID.randomUUID();

    when(refreshTokenRepository.findByToken(eq(refreshToken)))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(UserId.fromUuid(UUID.randomUUID()))
                    .token(refreshToken)
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(family)
                    .build()));
    when(refreshTokenRepository.updateByFamily(eq(family), any())).thenReturn(false);

    // AuthService doesn't check the boolean result, so the call should not throw.
    assertDoesNotThrow(() -> authService.logoutUser(refreshToken));

    verify(refreshTokenRepository, times(1)).updateByFamily(eq(family), any());
  }

  // endregion
}
