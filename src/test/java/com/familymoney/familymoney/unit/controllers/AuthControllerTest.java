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

  @Test
  void AuthController_Register_InvalidParams() {
    doNothing().when(authService).registerUser(any(), any(), any());
    client
        .post()
        .uri(String.format("%s/register", BASE_AUTH_URI))
        .body(new RegisterRequestDto("", FakeGenerator.email(), FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isBadRequest();
  }
}
