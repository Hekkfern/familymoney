package com.familymoney.familymoney.unit.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.controllers.AuthController;
import com.familymoney.familymoney.controllers.dtos.auth.LoginRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.RefreshTokenRequestDto;
import com.familymoney.familymoney.controllers.dtos.auth.RegisterRequestDto;
import com.familymoney.familymoney.repositories.IRoleRepository;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IAuthService;
import com.familymoney.familymoney.services.data.TokenPair;
import com.familymoney.familymoney.types.JwtToken;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.utils.AuthControllerUriFactory;
import com.familymoney.familymoney.utils.FakeGenerator;
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
  @MockitoBean private IRoleRepository permissionsRepository;

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindTo(mockMvc).build();
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
        Arguments.of("a" + "b".repeat(31), "long.user@example-domain.com", "Zz9@aaaaaaaaaaa"),
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
        .body(new RegisterRequestDto(username, email, password))
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
        .body(new RegisterRequestDto(username, FakeGenerator.email(), FakeGenerator.password()))
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
        .body(new RegisterRequestDto(FakeGenerator.username(), email, FakeGenerator.password()))
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
        .body(new RegisterRequestDto(FakeGenerator.username(), FakeGenerator.email(), password))
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
        .thenReturn(new TokenPair(new JwtToken("aa"), new RefreshToken("bb")));
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, password))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_EMAILS")
  void AuthController_Login_InvalidParam_Email(String email) {
    when(authService.loginUser(any(), any()))
        .thenReturn(new TokenPair(new JwtToken("aa"), new RefreshToken("bb")));
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(email, FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_PASSWORDS")
  void AuthController_Login_InvalidParam_Password(String password) {
    when(authService.loginUser(any(), any()))
        .thenReturn(new TokenPair(new JwtToken("aa"), new RefreshToken("bb")));
    client
        .post()
        .uri(AuthControllerUriFactory.getLoginPath())
        .body(new LoginRequestDto(FakeGenerator.email(), password))
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
                "nBErlAqusirf5ylhUWY65j3ortHBtaD75wxHQ4Q3yFE3jUnViVpyBtkEvvyXw1Yh"))
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
        .thenReturn(new TokenPair(new JwtToken("aa"), new RefreshToken("bb")));
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto(FakeGenerator.refreshToken()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void AuthController_Refresh_InvalidParam_Token() {
    when(authService.refreshTokens(any()))
        .thenReturn(new TokenPair(new JwtToken("aa"), new RefreshToken("bb")));
    client
        .post()
        .uri(AuthControllerUriFactory.getRefreshPath())
        .body(new RefreshTokenRequestDto("fdsfs!ffg231154"))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  // endregion
}
