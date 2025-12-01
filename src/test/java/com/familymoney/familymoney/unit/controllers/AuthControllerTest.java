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
        "",
        "a",
        "1",
        "1aaa",
        "a21354.asd",
        "thisusernameiswaytoolongtobevalid2s1af54saf54s5daf6s541f65as1"
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
}
