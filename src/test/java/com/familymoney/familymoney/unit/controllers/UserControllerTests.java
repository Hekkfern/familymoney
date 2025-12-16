package com.familymoney.familymoney.unit.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.controllers.UserController;
import com.familymoney.familymoney.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.FakeGenerator;
import com.familymoney.familymoney.utils.UserControllerUriFactory;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTests {

  private static final String ROLE_PREFIX = "ROLE_";

  // region Fields

  private RestTestClient client;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private IUserService userService;
  @MockitoBean private GetMyUserResponseMapper getMyUserResponseMapper;
  @MockitoBean private UpdateUserRequestMapper updateUserRequestMapper;

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindTo(mockMvc).build();
  }

  // region GET /me Tests

  @Test
  void AuthController_GetMyUserInfo_Successful() {
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                new GetUserData(
                    FakeGenerator.username(), FakeGenerator.email(), Instant.now(), true, true)));
    // Authenticate
    val authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + "USER"));
    val auth = new UsernamePasswordAuthenticationToken(FakeGenerator.userId(), null, authorities);
    SecurityContextHolder.getContext().setAuthentication(auth);

    // Request
    client.get().uri(UserControllerUriFactory.getMePath()).exchange().expectStatus().isOk();
  }

  @Test
  void AuthController_GetMyUserInfo_Unauthenticated() {
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                new GetUserData(
                    FakeGenerator.username(), FakeGenerator.email(), Instant.now(), true, true)));
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  // endregion
}
