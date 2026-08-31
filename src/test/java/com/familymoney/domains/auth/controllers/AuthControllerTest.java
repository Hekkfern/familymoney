package com.familymoney.domains.auth.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.controllers.mappers.LoginResponseMapper;
import com.familymoney.domains.auth.controllers.mappers.RefreshResponseMapper;
import com.familymoney.domains.auth.exceptions.BlacklistedFamilyException;
import com.familymoney.domains.auth.exceptions.EmailAlreadyVerifiedException;
import com.familymoney.domains.auth.exceptions.NewEmailVerificationTooSoonException;
import com.familymoney.domains.auth.exceptions.RefreshTokenInvalidException;
import com.familymoney.domains.auth.exceptions.RefreshTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.RefreshTokenReuseDetectedException;
import com.familymoney.domains.auth.exceptions.ResetPasswordTokenExpiredException;
import com.familymoney.domains.auth.exceptions.ResetPasswordTokenNotFoundException;
import com.familymoney.domains.auth.exceptions.UserAlreadyExistsException;
import com.familymoney.domains.auth.exceptions.UserNotEnabledException;
import com.familymoney.domains.auth.exceptions.VerificationTokenExpiredException;
import com.familymoney.domains.auth.exceptions.VerificationTokenNotFoundException;
import com.familymoney.domains.auth.services.AuthService;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.security.JwtUtils;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = DefaultAuthController.class)
@Import({JwtUtils.class, LoginResponseMapper.class, RefreshResponseMapper.class})
@AutoConfigureRestTestClient
class AuthControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  // region Fields

  @Autowired private RestTestClient client;

  @MockitoBean private AuthService authService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoBean private java.time.Clock clock;
  @MockitoBean private JwtUtils jwtUtils;
  @MockitoBean private UserService userService;

  // endregion

  @BeforeEach
  void setup() {
    when(jwtClock.now()).thenReturn(java.util.Date.from(now));
    when(clock.instant()).thenReturn(now);
  }

  @Nested
  class Register {

    private static Stream<Arguments> provideValidRegisterParams() {
      return Stream.of(
          // minimal valid password length (12), simple username
          Arguments.of("hector", "hector.fernandez+dev@example.com", "StrongPass1!"),
          // underscore in username, plus-addressing and multi-part TLD
          Arguments.of("user_123", "user+tag@example.co.uk", "Aa1$aaaaaaaa"),
          // hyphen in username, dot in local-part
          Arguments.of("john-doe", "john.doe@example.com", "Password123$!"),
          // max-length username (32 chars): 'a' + 31 'b'
          Arguments.of(
              "abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
              "long.user@example-domain.com",
              "Zz9@aaaaaaaaaaa"),
          // another valid combination with mixed allowed specials in password (ensure >=12 chars)
          Arguments.of("alpha1", "alpha1@mail.example.org", "GoodPass1@$a"));
    }

    @ParameterizedTest
    @MethodSource("provideValidRegisterParams")
    void success(String username, String email, String password) {
      doNothing().when(authService).registerUser(any(), any(), any());
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(Map.of("username", username, "email", email, "password", password))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    void conflict_when_user_identity_already_exists() {
      doThrow(new UserAlreadyExistsException("User already exists"))
          .when(authService)
          .registerUser(any(), any(), any());

      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(
              Map.of(
                  "username",
                  FakeGenerator.username(),
                  "email",
                  FakeGenerator.email(),
                  "password",
                  FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isEqualTo(409);
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_USERNAMES")
    @EmptySource
    void badrequest_when_username_is_invalid(String username) {
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(
              Map.of(
                  "username",
                  username,
                  "email",
                  FakeGenerator.email(),
                  "password",
                  FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_username_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(Map.of("email", FakeGenerator.email(), "password", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILS")
    @EmptySource
    void badrequest_when_email_is_invalid(String email) {
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(
              Map.of(
                  "username",
                  FakeGenerator.username(),
                  "email",
                  email,
                  "password",
                  FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_email_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(Map.of("username", FakeGenerator.username(), "password", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_PASSWORDS")
    @EmptySource
    void badrequest_when_password_is_invalid(String password) {
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(
              Map.of(
                  "username",
                  FakeGenerator.username(),
                  "email",
                  FakeGenerator.email(),
                  "password",
                  password))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_password_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getRegisterPath())
          .body(Map.of("email", FakeGenerator.email(), "username", FakeGenerator.username()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }

  @Nested
  class Login {

    private static Stream<Arguments> provideValidLoginParams() {
      return Stream.of(
          // minimal valid password length (12), simple username
          Arguments.of("hector.fernandez+dev@example.com", "StrongPass1!"),
          // underscore in username, plus-addressing and multi-part TLD
          Arguments.of("user+tag@example.co.uk", "Aa1$aaaaaaaa"),
          // hyphen in username, dot in local-part
          Arguments.of("john.doe@example.com", "Password123$!"),
          // max-length username (32 chars): 'a' + 31 'b'
          Arguments.of("long.user@example-domain.com", "Zz9@aaaaaaaaaaa"),
          // another valid combination with mixed allowed specials in password (ensure >=12 chars)
          Arguments.of("alpha1@mail.example.org", "GoodPass1@$a"));
    }

    @ParameterizedTest
    @MethodSource("provideValidLoginParams")
    void success(String email, String password) {
      when(authService.loginUser(any(), any()))
          .thenReturn(
              new TokenPair(
                  AccessToken.fromString(FakeGenerator.accessToken()), RefreshToken.generate()));
      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("email", email, "password", password))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    void unauthorized_when_credentials_are_rejected() {
      when(authService.loginUser(any(), any()))
          .thenThrow(new BadCredentialsException("Account does not exist"));

      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("email", FakeGenerator.email(), "password", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILS")
    @EmptySource
    void badrequest_when_email_is_invalid(String email) {
      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("email", email, "password", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_email_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("password", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_PASSWORDS")
    @EmptySource
    void badrequest_when_password_is_invalid(String password) {
      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("email", FakeGenerator.email(), "password", password))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_password_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("email", FakeGenerator.email()))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }

  @Nested
  class VerifyEmail {

    private static Stream<Arguments> provideExpectedFailures() {
      return Stream.of(
          Arguments.of(new VerificationTokenNotFoundException("Verification token not found"), 404),
          Arguments.of(new VerificationTokenExpiredException("Verification token expired"), 410),
          Arguments.of(new EmailAlreadyVerifiedException("Email already verified"), 409));
    }

    @Test
    void success() {
      doNothing().when(authService).verifyEmail(any());
      client
          .post()
          .uri(AuthControllerUriFactory.getVerifyEmailPath())
          .body(Map.of("token", FakeGenerator.emailVerificationToken()))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @ParameterizedTest
    @MethodSource("provideExpectedFailures")
    void returns_expected_status_for_verification_failure(
        final RuntimeException exception, final int expectedStatus) {
      doThrow(exception).when(authService).verifyEmail(any());

      client
          .post()
          .uri(AuthControllerUriFactory.getVerifyEmailPath())
          .body(Map.of("token", FakeGenerator.emailVerificationToken()))
          .exchange()
          .expectStatus()
          .isEqualTo(expectedStatus);
    }

    @Test
    void badrequest_when_email_verification_token_is_invalid() {
      client
          .post()
          .uri(AuthControllerUriFactory.getVerifyEmailPath())
          .body(Map.of("token", "1a!"))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_email_verification_token_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getVerifyEmailPath())
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }

  @Nested
  class ResendVerificationEmail {

    @Test
    void accepted_when_email_is_valid() {
      doNothing().when(authService).resendVerificationEmail(any());
      client
          .post()
          .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
          .body(Map.of("email", FakeGenerator.email()))
          .exchange()
          .expectStatus()
          .isAccepted();

      verify(authService).resendVerificationEmail(any());
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILS")
    @EmptySource
    void badrequest_when_email_is_invalid(final String email) {
      client
          .post()
          .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
          .body(Map.of("email", email))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(authService);
    }

    @Test
    void badrequest_when_email_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
          .body(Map.of())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(authService);
    }

    @Test
    void too_many_requests_when_resend_is_rate_limited() {
      final Instant resetTime = now.plusSeconds(60);
      doThrow(new NewEmailVerificationTooSoonException(resetTime))
          .when(authService)
          .resendVerificationEmail(any());

      client
          .post()
          .uri(AuthControllerUriFactory.getResendVerificationEmailPath())
          .body(Map.of("email", FakeGenerator.email()))
          .exchange()
          .expectStatus()
          .isEqualTo(429)
          .expectHeader()
          .valueEquals("Retry-After", "60")
          .expectHeader()
          .valueEquals("RateLimit-Limit", "1")
          .expectHeader()
          .valueEquals("RateLimit-Remaining", "0")
          .expectHeader()
          .valueEquals("RateLimit-Reset", String.valueOf(resetTime.getEpochSecond()));
    }
  }

  @Nested
  class ResetPassword {

    private static Stream<Arguments> provideExpectedFailures() {
      return Stream.of(
          Arguments.of(
              new ResetPasswordTokenNotFoundException("Password reset token not found"), 404),
          Arguments.of(
              new ResetPasswordTokenExpiredException("Password reset token expired"), 410));
    }

    @Test
    void success() {
      final String token = PasswordResetToken.generate().value();

      client
          .post()
          .uri(AuthControllerUriFactory.getResetPasswordPath())
          .body(Map.of("token", token, "newPassword", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(authService).resetPassword(any(), any());
    }

    @ParameterizedTest
    @MethodSource("provideExpectedFailures")
    void returns_expected_status_for_reset_failure(
        final RuntimeException exception, final int expectedStatus) {
      doThrow(exception).when(authService).resetPassword(any(), any());

      client
          .post()
          .uri(AuthControllerUriFactory.getResetPasswordPath())
          .body(
              Map.of(
                  "token",
                  PasswordResetToken.generate().value(),
                  "newPassword",
                  FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isEqualTo(expectedStatus);
    }

    @Test
    void badrequest_when_password_reset_token_has_legacy_length() {
      client
          .post()
          .uri(AuthControllerUriFactory.getResetPasswordPath())
          .body(Map.of("token", "a".repeat(32), "newPassword", FakeGenerator.password()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(authService);
    }
  }

  @Nested
  class Refresh {

    private static Stream<RuntimeException> provideExpectedFailures() {
      return Stream.of(
          new RefreshTokenNotFoundException("Refresh token not found"),
          new RefreshTokenInvalidException("Refresh token expired"),
          new RefreshTokenReuseDetectedException(),
          new BlacklistedFamilyException("Token family is blacklisted"),
          new UserNotEnabledException());
    }

    @Test
    void success() {
      when(authService.refreshTokens(any()))
          .thenReturn(
              new TokenPair(
                  AccessToken.fromString(FakeGenerator.accessToken()), RefreshToken.generate()));
      client
          .post()
          .uri(AuthControllerUriFactory.getRefreshPath())
          .body(Map.of("refreshToken", FakeGenerator.refreshToken()))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @ParameterizedTest
    @MethodSource("provideExpectedFailures")
    void unauthorized_when_refresh_token_is_rejected(final RuntimeException exception) {
      when(authService.refreshTokens(any())).thenThrow(exception);

      client
          .post()
          .uri(AuthControllerUriFactory.getRefreshPath())
          .body(Map.of("refreshToken", FakeGenerator.refreshToken()))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }

    @Test
    void badrequest_when_refresh_token_is_invalid() {
      client
          .post()
          .uri(AuthControllerUriFactory.getRefreshPath())
          .body(Map.of("refreshToken", "fdsfs!ffg231154"))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_refresh_token_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getRefreshPath())
          .body(Map.of())
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }

  @Nested
  class Logout {

    @Test
    void success() {
      doNothing().when(authService).logoutUser(any());
      client
          .post()
          .uri(AuthControllerUriFactory.getLogoutPath())
          .body(Map.of("refreshToken", FakeGenerator.refreshToken()))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    void unauthorized_when_refresh_token_does_not_exist() {
      doThrow(new RefreshTokenNotFoundException("Refresh token not found"))
          .when(authService)
          .logoutUser(any());

      client
          .post()
          .uri(AuthControllerUriFactory.getLogoutPath())
          .body(Map.of("refreshToken", FakeGenerator.refreshToken()))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }

    @Test
    void badrequest_when_refresh_token_is_invalid() {
      client
          .post()
          .uri(AuthControllerUriFactory.getLogoutPath())
          .body(Map.of("refreshToken", "fdsfs!ffg231154"))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_refresh_token_is_missing() {
      client
          .post()
          .uri(AuthControllerUriFactory.getLogoutPath())
          .body(Map.of())
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }
}
