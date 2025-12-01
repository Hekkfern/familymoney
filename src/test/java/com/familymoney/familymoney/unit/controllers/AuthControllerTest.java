package com.familymoney.familymoney.unit.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.familymoney.familymoney.controllers.AuthController;
import com.familymoney.familymoney.dtos.auth.RegisterRequestDto;
import com.familymoney.familymoney.repositories.IPermissionsRepository;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IAuthService;
import com.familymoney.familymoney.utils.FakeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = AuthController.class)
public class AuthControllerTest {

  // region Fields

  private RestTestClient client;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private IAuthService authService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private IPermissionsRepository permissionsRepository;

  private final String BASE_AUTH_URI = "/api/auth";

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindTo(mockMvc).build();
  }

  @Test
  void AuthController_Register_CorrectParams() {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(String.format("%s/register", BASE_AUTH_URI))
        .body(
            new RegisterRequestDto(
                FakeGenerator.username(), FakeGenerator.email(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "", // empty
        "a", // length 1 (too short)
        "ab", // length 2 (too short)
        "1abc", // starts with digit
        "-abc", // starts with hyphen
        "_abc", // starts with underscore
        "Abc", // starts with uppercase letter
        "aBc", // contains uppercase letter
        "a21354.asd", // contains dot
        "user name", // contains space
        "user@name", // contains @
        "user,name", // contains comma
        "user$money", // contains $
        "thisusernameiswaytoolongtobevalid2s1af54saf54s5daf6s541f65as1", // >32 chars (too long)
      })
  void AuthController_Register_InvalidParam_Username(String username) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(String.format("%s/register", BASE_AUTH_URI))
        .body(new RegisterRequestDto(username, FakeGenerator.email(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "", // empty
        "plainaddress", // missing @ and domain
        "@no-local-part.com", // missing local part
        "no-at.domain.com", // missing @
        "user@.com", // dot immediately after @
        "user@domain..com", // consecutive dots in domain
        "user@@domain.com", // double @
        "user@domain,com", // comma instead of dot
        " user@domain.com" // leading space
      })
  void AuthController_Register_InvalidParam_Email(String email) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(String.format("%s/register", BASE_AUTH_URI))
        // keep username valid and vary email with the parameter under test
        .body(new RegisterRequestDto(FakeGenerator.username(), email, FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Short1!", // too short (<12)
        "alllowercase1!", // missing uppercase
        "ALLUPPERCASE1!", // missing lowercase
        "NoDigitPassword!", // missing digit
        "NoSpecialChar1A", // missing special character
        "Invalid#Char1A", // '#' not allowed in charset
        "Contains Space1!", // spaces are invalid
        "\tTabInPassword1!", // control character (tab)
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", // >64 chars
      })
  void AuthController_Register_InvalidParam_Password(String password) {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(String.format("%s/register", BASE_AUTH_URI))
        // keep username and email valid; vary password
        .body(new RegisterRequestDto(FakeGenerator.username(), FakeGenerator.email(), password))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }
}
