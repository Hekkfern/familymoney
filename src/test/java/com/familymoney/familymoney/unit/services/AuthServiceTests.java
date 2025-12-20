package com.familymoney.familymoney.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class AuthServiceTests {

  @Mock private IUserRepository userRepository;
  @Mock private IRoleRepository permissionsRepository;
  @Mock private IEmailSenderService emailSenderService;
  @Spy private UserPasswordEncoder passwordEncoder;
  @Mock private IEmailVerificationRepository emailVerificationRepository;
  @Mock private JwtUtil jwtUtil;
  @Mock private IRefreshTokenRepository refreshTokenRepository;

  @InjectMocks private AuthService authService;

  // region registerUser()

  @Test
  void register_user_for_the_first_time_it_succeeds() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(false);
    when(userRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                new UserDbo(
                    FakeGenerator.userId(),
                    FakeGenerator.username(),
                    FakeGenerator.email(),
                    "dsafjhadskjgf5dsf56a4",
                    Instant.now(),
                    Instant.now(),
                    false,
                    true)));
    when(emailVerificationRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                new EmailVerificationDbo(
                    UUID.randomUUID(),
                    FakeGenerator.userId(),
                    FakeGenerator.emailVerificationToken(),
                    Instant.now(),
                    Instant.now().plusSeconds(3600))));

    assertDoesNotThrow(
        () ->
            authService.registerUser(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()));

    verify(userRepository, times(1)).create(any(), any(), any());
    verify(emailVerificationRepository, times(1)).create(any(), any(), any());
    verify(permissionsRepository, times(1)).setRoleForUserId(any(), any());
  }

  @Test
  void register_user_when_it_already_exists() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(true);

    assertThrows(
        UserAlreadyExistsException.class,
        () ->
            authService.registerUser(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()));
  }

  @Test
  void register_user_but_user_table_fails() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(false);
    when(userRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () ->
            authService.registerUser(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()));
  }

  @Test
  void register_user_but_email_verification_table_fails() {
    when(userRepository.existsByEmailOrUsername(any(), any())).thenReturn(false);
    when(userRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                new UserDbo(
                    FakeGenerator.userId(),
                    FakeGenerator.username(),
                    FakeGenerator.email(),
                    "dsafjhadskjgf5dsf56a4",
                    Instant.now(),
                    Instant.now(),
                    false,
                    true)));
    when(emailVerificationRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(
        DatabaseExecutionException.class,
        () ->
            authService.registerUser(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()));
  }

  // endregion

  // region loginUser()

  @Test
  void login_user_with_correct_credentials_succeeds() {
    final Email email = FakeGenerator.email();
    final Password password = FakeGenerator.password();

    final UserDbo userDb =
        new UserDbo(
            FakeGenerator.userId(),
            FakeGenerator.username(),
            email,
            "hashed-password",
            Instant.now(),
            Instant.now(),
            true,
            true);

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(userDb));
    // password check passes
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    // jwt generation
    when(jwtUtil.generateAccessToken(any())).thenReturn(FakeGenerator.accessToken());
    // refresh token persisted successfully
    when(refreshTokenRepository.create(any(), any(), any()))
        .thenReturn(
            Optional.of(
                RefreshTokenDbo.builder()
                    .id(UUID.randomUUID())
                    .userId(userDb.id())
                    .token(FakeGenerator.refreshToken())
                    .createdAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .isUsed(false)
                    .usedAt(Optional.empty())
                    .family(UUID.randomUUID())
                    .build()));

    assertDoesNotThrow(
        () -> {
          final var tokens = authService.loginUser(email, password);
          assertNotNull(tokens);
          assertNotNull(tokens.accessToken());
          assertNotNull(tokens.refreshToken());
        });

    verify(refreshTokenRepository, times(1)).create(any(), any(), any());
  }

  @Test
  void login_user_with_incorrect_credentials_fails() {
    final Email email = FakeGenerator.email();
    final Password password = FakeGenerator.password();

    final UserDbo userDb =
        new UserDbo(
            FakeGenerator.userId(),
            FakeGenerator.username(),
            email,
            "hashed-password",
            Instant.now(),
            Instant.now(),
            true,
            true);

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(userDb));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_that_does_not_exist_fails() {
    final Email email = FakeGenerator.email();
    final Password password = FakeGenerator.password();

    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_but_refreshtoken_table_fails() {
    final Email email = FakeGenerator.email();
    final Password password = FakeGenerator.password();

    final UserDbo userDb =
        new UserDbo(
            FakeGenerator.userId(),
            FakeGenerator.username(),
            email,
            "hashed-password",
            Instant.now(),
            Instant.now(),
            true,
            true);

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(userDb));
    when(passwordEncoder.verify(eq(password.value()), anyString())).thenReturn(true);
    when(jwtUtil.generateAccessToken(any())).thenReturn(FakeGenerator.accessToken());
    when(refreshTokenRepository.create(any(), any(), any())).thenReturn(Optional.empty());

    assertThrows(DatabaseExecutionException.class, () -> authService.loginUser(email, password));
  }

  @Test
  void login_user_with_unverified_email_fails() {
    final Email email = FakeGenerator.email();
    final Password password = FakeGenerator.password();

    final UserDbo userDb =
        new UserDbo(
            FakeGenerator.userId(),
            FakeGenerator.username(),
            email,
            "hashed-password",
            Instant.now(),
            Instant.now(),
            false,
            true);

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(userDb));

    assertThrows(BadCredentialsException.class, () -> authService.loginUser(email, password));
  }

  // endregion

    // region refreshTokens()

    @Test
    void refresh_tokens_with_valid_refresh_token_succeeds() {

    }

    @Test
    void refresh_tokens_with_non_existing_refresh_token_fails() {

    }

    @Test
    void refresh_tokens_with_used_refresh_token_fails() {

    }

    @Test
    void refresh_tokens_but_refresh_token_table_fails() {

    }

    @Test
    void refresh_tokens_with_expired_refresh_token_fails() {

    }

    //endregion
}
