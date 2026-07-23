package com.familymoney.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.controllers.AuthController;
import com.familymoney.domains.auth.controllers.mappers.LoginResponseMapper;
import com.familymoney.domains.auth.controllers.mappers.RefreshResponseMapper;
import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.security.JwtUtils;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = AuthController.class)
@Import({JwtUtils.class, LoginResponseMapper.class, RefreshResponseMapper.class})
@AutoConfigureRestTestClient
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  // region Fields

  @Autowired private RestTestClient client;

  @MockitoBean private IAuthService authService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoBean private JwtUtils jwtUtils;
  @MockitoBean private IUserService userService;

  // endregion

  @BeforeEach
  void setup() {
    when(jwtClock.now()).thenReturn(java.util.Date.from(now));
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

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_USERNAMES")
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
                  AccessToken.fromString(FakeGenerator.accessToken()),
                  RefreshToken.fromString(FakeGenerator.refreshToken())));
      client
          .post()
          .uri(AuthControllerUriFactory.getLoginPath())
          .body(Map.of("email", email, "password", password))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @ParameterizedTest
    @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILS")
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

    @Test
    void success() {
      doNothing().when(authService).verifyEmail(any());
      client
          .get()
          .uri(AuthControllerUriFactory.getVerifyEmailPath(FakeGenerator.emailVerificationToken()))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    void badrequest_when_email_verification_token_is_invalid() {
      client
          .get()
          .uri(AuthControllerUriFactory.getVerifyEmailPath("nBErlAqusirf5!ylhUWY65j+)1Yh"))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    void badrequest_when_email_verification_token_is_missing() {
      client
          .get()
          .uri(AuthControllerUriFactory.getVerifyEmailPath(""))
          .exchange()
          .expectStatus()
          .isNotFound();
    }
  }

  @Nested
  class Refresh {

    @Test
    void success() {
      when(authService.refreshTokens(any()))
          .thenReturn(
              new TokenPair(
                  AccessToken.fromString(FakeGenerator.accessToken()),
                  RefreshToken.fromString(FakeGenerator.refreshToken())));
      client
          .post()
          .uri(AuthControllerUriFactory.getRefreshPath())
          .body(Map.of("refreshToken", FakeGenerator.refreshToken()))
          .exchange()
          .expectStatus()
          .isOk();
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
