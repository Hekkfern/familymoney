package com.familymoney.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.controllers.AuthController;
import com.familymoney.domains.auth.controllers.mappers.LoginResponseMapper;
import com.familymoney.domains.auth.controllers.mappers.RefreshResponseMapper;
import com.familymoney.security.JwtUtils;
import com.familymoney.domains.auth.services.IAuthService;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.testutils.AuthControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = AuthController.class)
@Import({JwtUtils.class, LoginResponseMapper.class, RefreshResponseMapper.class})
@AutoConfigureRestTestClient
class AuthControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  // region Fields

  @Autowired private RestTestClient client;

  @MockitoBean private IAuthService authService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoBean private JwtUtils jwtUtils;
  @MockitoBean private IUserService userService;
  @MockitoSpyBean private LoginResponseMapper loginResponseMapper;
  @MockitoSpyBean private RefreshResponseMapper refreshResponseMapper;

  // endregion

  @BeforeEach
  void setup() {
    when(jwtClock.now()).thenReturn(java.util.Date.from(now));
  }

  // region /register Tests

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
            "abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "long.user@example-domain.com", "Zz9@aaaaaaaaaaa"),
        // another valid combination with mixed allowed specials in password (ensure >=12 chars)
        Arguments.of("alpha1", "alpha1@mail.example.org", "GoodPass1@$a"));
  }

  @ParameterizedTest
  @MethodSource("provideValidRegisterParams")
  void AuthController_Register_Successful(String username, String email, String password) {
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
  @FieldSource("com.familymoney.utils.TestDataFactory#INVALID_USERNAMES")
  void AuthController_Register_InvalidParam_Username(String username) {
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
  void AuthController_Register_MissingParam_Username() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(Map.of("email", FakeGenerator.email(), "password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.utils.TestDataFactory#INVALID_EMAILS")
  void AuthController_Register_InvalidParam_Email(String email) {
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
  void AuthController_Register_MissingParam_Email() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(Map.of("username", FakeGenerator.username(), "password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.utils.TestDataFactory#INVALID_PASSWORDS")
  void AuthController_Register_InvalidParam_Password(String password) {
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
  void AuthController_Register_MissingParam_Password() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(Map.of("email", FakeGenerator.email(), "username", FakeGenerator.username()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion

  // region /login Tests

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
  void AuthController_Login_Successful(String email, String password) {
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
  @FieldSource("com.familymoney.utils.TestDataFactory#INVALID_EMAILS")
  void AuthController_Login_InvalidParam_Email(String email) {
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("email", email, "password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void AuthController_Login_MissingParam_Email() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.utils.TestDataFactory#INVALID_PASSWORDS")
  void AuthController_Login_InvalidParam_Password(String password) {
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("email", FakeGenerator.email(), "password", password))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void AuthController_Login_MissingParam_Password() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("email", FakeGenerator.email()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion

  // region /verify-email/{token} Tests

  @Test
  void AuthController_VerifyEmail_Successful() {
    doNothing().when(authService).verifyEmail(any());
    client
        .get()
        .uri(AuthControllerUriFactory.getVerifyEmailPath(FakeGenerator.emailVerificationToken()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void AuthController_VerifyEmail_InvalidParam_Token() {
    client
        .get()
        .uri(AuthControllerUriFactory.getVerifyEmailPath("nBErlAqusirf5!ylhUWY65j+)1Yh"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void AuthController_VerifyEmail_MissingParam_Token() {
    client
        .get()
        .uri(AuthControllerUriFactory.getVerifyEmailPath(""))
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  // endregion

  // region /refresh Tests

  @Test
  void AuthController_Refresh_Successful() {
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
  void AuthController_Refresh_InvalidParam_Token() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(Map.of("refreshToken", "fdsfs!ffg231154"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void AuthController_Refresh_MissingParam_Token() {
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(Map.of())
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion

  // region /logout Tests

  @Test
  void AuthController_Logout_Successful() {
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
  void AuthController_Logout_InvalidParam_Token() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(Map.of("refreshToken", "fdsfs!ffg231154"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void AuthController_Logout_MissingParam_Token() {
    client
        .post()
        .uri(AuthControllerUriFactory.getLogoutPath())
        .body(Map.of())
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion
}
