package com.familymoney.familymoney.unit.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.controllers.AuthController;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IAuthService;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.services.data.TokenPair;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.AuthControllerUriFactory;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = AuthController.class)
public class AuthControllerTests {

  // region Fields

  private RestTestClient client;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private IAuthService authService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private IUserService userService;

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindTo(mockMvc).build();
  }

  // region /register Tests

  private static Stream<Arguments> provideValidRegisterParams() {
    return Stream.of(
        // minimal valid password length (12), simple username
        Arguments.of(
            Username.fromString("hector"),
            Email.fromString("hector.fernandez+dev@example.com"),
            Password.fromString("StrongPass1!")),
        // underscore in username, plus-addressing and multi-part TLD
        Arguments.of(
            Username.fromString("user_123"),
            Email.fromString("user+tag@example.co.uk"),
            Password.fromString("Aa1$aaaaaaaa")),
        // hyphen in username, dot in local-part
        Arguments.of(
            Username.fromString("john-doe"),
            Email.fromString("john.doe@example.com"),
            Password.fromString("Password123$!")),
        // max-length username (32 chars): 'a' + 31 'b'
        Arguments.of(
            Username.fromString("abbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
            Email.fromString("long.user@example-domain.com"),
            Password.fromString("Zz9@aaaaaaaaaaa")),
        // another valid combination with mixed allowed specials in password (ensure >=12 chars)
        Arguments.of(
            Username.fromString("alpha1"),
            Email.fromString("alpha1@mail.example.org"),
            Password.fromString("GoodPass1@$a")));
  }

  @ParameterizedTest
  @MethodSource("provideValidRegisterParams")
  void AuthController_Register_Successful(Username username, Email email, Password password) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(
            Map.of(
                "username",
                username.toString(),
                "email",
                email.toString(),
                "password",
                password.toString()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_USERNAMES")
  void AuthController_Register_InvalidParam_Username(String username) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(
            Map.of(
                "username",
                username,
                "email",
                FakeGenerator.email().toString(),
                "password",
                FakeGenerator.password().toString()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_EMAILS")
  void AuthController_Register_InvalidParam_Email(String email) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(
            Map.of(
                "username",
                FakeGenerator.username().toString(),
                "email",
                email,
                "password",
                FakeGenerator.password().toString()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_PASSWORDS")
  void AuthController_Register_InvalidParam_Password(String password) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(AuthControllerUriFactory.getRegisterPath())
        .body(
            Map.of(
                "username",
                FakeGenerator.username().toString(),
                "email",
                FakeGenerator.email().toString(),
                "password",
                password))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion

  // region /login Tests

  private static Stream<Arguments> provideValidLoginParams() {
    return Stream.of(
        // minimal valid password length (12), simple username
        Arguments.of(
            Email.fromString("hector.fernandez+dev@example.com"),
            Password.fromString("StrongPass1!")),
        // underscore in username, plus-addressing and multi-part TLD
        Arguments.of(
            Email.fromString("user+tag@example.co.uk"), Password.fromString("Aa1$aaaaaaaa")),
        // hyphen in username, dot in local-part
        Arguments.of(
            Email.fromString("john.doe@example.com"), Password.fromString("Password123$!")),
        // max-length username (32 chars): 'a' + 31 'b'
        Arguments.of(
            Email.fromString("long.user@example-domain.com"),
            Password.fromString("Zz9@aaaaaaaaaaa")),
        // another valid combination with mixed allowed specials in password (ensure >=12 chars)
        Arguments.of(
            Email.fromString("alpha1@mail.example.org"), Password.fromString("GoodPass1@$a")));
  }

  @ParameterizedTest
  @MethodSource("provideValidLoginParams")
  void AuthController_Login_Successful(Email email, Password password) {
    when(authService.loginUser(any(), any()))
        .thenReturn(new TokenPair(FakeGenerator.accessToken(), FakeGenerator.refreshToken()));
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("email", email.toString(), "password", password.toString()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_EMAILS")
  void AuthController_Login_InvalidParam_Email(String email) {
    when(authService.loginUser(any(), any()))
        .thenReturn(new TokenPair(FakeGenerator.accessToken(), FakeGenerator.refreshToken()));
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("email", email, "password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_PASSWORDS")
  void AuthController_Login_InvalidParam_Password(String password) {
    when(authService.loginUser(any(), any()))
        .thenReturn(new TokenPair(FakeGenerator.accessToken(), FakeGenerator.refreshToken()));
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(Map.of("email", FakeGenerator.email().toString(), "password", password))
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
        .uri(
            AuthControllerUriFactory.getVerifyEmailPath(
                FakeGenerator.emailVerificationToken().toString()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void AuthController_VerifyEmail_InvalidParam_Token() {
    doNothing().when(authService).verifyEmail(any());
    client
        .get()
        .uri(AuthControllerUriFactory.getVerifyEmailPath("nBErlAqusirf5!ylhUWY65j+)1Yh"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion

  // region /refresh Tests

  @Test
  void AuthController_Refresh_Successful() {
    when(authService.refreshTokens(any()))
        .thenReturn(new TokenPair(FakeGenerator.accessToken(), FakeGenerator.refreshToken()));
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(Map.of("refreshToken", FakeGenerator.refreshToken().toString()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void AuthController_Refresh_InvalidParam_Token() {
    when(authService.refreshTokens(any()))
        .thenReturn(new TokenPair(FakeGenerator.accessToken(), FakeGenerator.refreshToken()));
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(Map.of("refreshToken", "fdsfs!ffg231154"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion
}
