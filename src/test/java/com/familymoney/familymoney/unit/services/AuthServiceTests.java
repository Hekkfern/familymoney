package com.familymoney.familymoney.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.RefreshTokenInvalidException;
import com.familymoney.familymoney.exceptions.RefreshTokenNotFoundException;
import com.familymoney.familymoney.exceptions.UserAlreadyExistsException;
import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import com.familymoney.familymoney.repositories.IRefreshTokenRepository;
import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.repositories.IUserRepository;
import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.security.UserPasswordEncoder;
import com.familymoney.familymoney.services.AuthService;
import com.familymoney.familymoney.services.IEmailSenderService;
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
  @Mock private Clock clock;
  @Mock private IEmailVerificationRepository emailVerificationRepository;
  @Mock private JwtUtil jwtUtil;
  @Mock private IRefreshTokenRepository refreshTokenRepository;

  @InjectMocks private AuthService authService;

  @BeforeEach
  void setupClock() {
    lenient().when(clock.instant()).thenReturn(now);
    lenient().when(clock.getZone()).thenReturn(ZoneOffset.UTC);
  }

  // region registerUser()

  @Test
  void register_user_for_the_first_time_it_succeeds() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    val userId = FakeGenerator.userId();

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
                    .token(FakeGenerator.emailVerificationToken())
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
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();

    when(userRepository.existsByEmailOrUsername(eq(email), eq(username))).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class,
        () -> authService.registerUser(username, email, password));
  }

  @Test
  void register_user_but_user_table_fails() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();

    when(userRepository.existsByEmailOrUsername(eq(email), eq(username))).thenReturn(false);
    when(userRepository.create(eq(username), eq(email), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () -> authService.registerUser(username, email, password));
  }

  @Test
  void register_user_but_email_verification_table_fails() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    val userId = FakeGenerator.userId();

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
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    val userId = FakeGenerator.userId();

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(FakeGenerator.username())
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtil.generateAccessToken(eq(userId))).thenReturn(FakeGenerator.accessToken());
    when(refreshTokenRepository.create(eq(userId), any(), any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(FakeGenerator.refreshToken())
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
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(FakeGenerator.userId())
                    .username(FakeGenerator.username())
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
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_but_refreshtoken_table_fails() {
    val email = FakeGenerator.email();
    val password = FakeGenerator.password();
    val userId = FakeGenerator.userId();

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(FakeGenerator.username())
                    .email(email)
                    .hashedPassword("hashed-password")
                    .createdAt(now)
                    .updatedAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtil.generateAccessToken(eq(userId))).thenReturn(FakeGenerator.accessToken());
    when(refreshTokenRepository.create(eq(userId), any(), any())).thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_with_unverified_email_fails() {
    final Email email = FakeGenerator.email();
    final Password password = FakeGenerator.password();

    when(userRepository.findByEmail(any()))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(FakeGenerator.userId())
                    .username(FakeGenerator.username())
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
    final RefreshToken token = FakeGenerator.refreshToken();
    final var userId = FakeGenerator.userId();
    final var family = UUID.randomUUID();

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
    when(jwtUtil.generateAccessToken(any())).thenReturn(FakeGenerator.accessToken());
    when(refreshTokenRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .token(FakeGenerator.refreshToken())
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
        () -> authService.refreshTokens(FakeGenerator.refreshToken()));
  }

  @Test
  void refresh_tokens_with_used_refresh_token_fails() {
    final RefreshToken token = FakeGenerator.refreshToken();
    final var userId = FakeGenerator.userId();
    final var family = UUID.randomUUID();

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
                    .username(FakeGenerator.username())
                    .email(FakeGenerator.email())
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
    final RefreshToken token = FakeGenerator.refreshToken();
    final var userId = FakeGenerator.userId();
    final var family = UUID.randomUUID();

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
    when(jwtUtil.generateAccessToken(any())).thenReturn(FakeGenerator.accessToken());
    when(refreshTokenRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.refreshTokens(token));
  }

  @Test
  void refresh_tokens_with_expired_refresh_token_fails() {
    final RefreshToken token = FakeGenerator.refreshToken();
    final var userId = FakeGenerator.userId();
    final var family = UUID.randomUUID();

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
    val token = FakeGenerator.emailVerificationToken();
    val userId = FakeGenerator.userId();

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
    val token = FakeGenerator.emailVerificationToken();
    val userId = FakeGenerator.userId();

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
  void verify_email_with_non_existing_token_fails() {}

  @Test
  void verify_email_with_expired_token_fails() {}

  @Test
  void verify_email_but_user_repository_fails() {}

  @Test
  void verify_email_but_email_verification_repository_fails() {}

  // endregion

  // region resendVerificationEmail()

  @Test
  void resend_verification_email_succeeds() {
    val email = FakeGenerator.email();
    val userId = FakeGenerator.userId();

    when(userRepository.findByEmail(eq(email)))
        .thenReturn(
            Optional.of(
                UserDbo.builder()
                    .id(userId)
                    .username(FakeGenerator.username())
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
                    .token(FakeGenerator.emailVerificationToken())
                    .createdAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build()));

    assertDoesNotThrow(() -> authService.resendVerificationEmail(email));

    verify(emailVerificationRepository, times(1)).create(eq(userId), any(), any());
  }

  @Test
  void resend_verification_email_for_verified_email_fails() {}

  // endregion

  // region logoutUser()

  @Test
  void logout_user_succeeds() {
    val refreshToken = FakeGenerator.refreshToken();
    val userId = FakeGenerator.userId();
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
  void logout_user_with_non_existing_refresh_token_fails() {}

  @Test
  void logout_user_with_already_used_refresh_token_fails() {}

  @Test
  void logout_user_with_expired_refresh_token_fails() {}

  @Test
  void logout_user_but_refresh_token_repository_fails() {}

  // endregion
}
