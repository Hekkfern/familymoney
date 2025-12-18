package com.familymoney.familymoney.unit.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.controllers.UserController;
import com.familymoney.familymoney.controllers.dtos.user.GetMyUserResponseDto;
import com.familymoney.familymoney.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.familymoney.controllers.mappers.GetMyUserResponseMapperImpl;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.familymoney.controllers.mappers.UpdateUserRequestMapperImpl;
import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.FakeGenerator;
import com.familymoney.familymoney.utils.UserControllerUriFactory;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = UserController.class,
    properties = {
      "spring.application.name=testapp",
      "jwt.key=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
@Import({JwtUtil.class, GetMyUserResponseMapperImpl.class, UpdateUserRequestMapperImpl.class})
@EnableConfigurationProperties({AppProperties.class, JwtProperties.class})
public class UserControllerTests {

  private static final String ROLE_PREFIX = "ROLE_";

  // region Fields

  private RestTestClient client;

  @Autowired private MockMvc mockMvc;

  @MockitoSpyBean private JwtUtil jwtUtil;
  @MockitoBean private IUserService userService;
  @Spy private GetMyUserResponseMapper getMyUserResponseMapper;
  @Spy private UpdateUserRequestMapper updateUserRequestMapper;

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindTo(mockMvc).build();
  }

  // region GET /me Tests

  @Test
  void UserController_GetMyUserInfo_Successful() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    val data =
        client
            .get()
            .uri(UserControllerUriFactory.getMePath())
            .header(
                "Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GetMyUserResponseDto.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(data);
    assertEquals(username, data.username());
    assertEquals(email, data.email());
  }

  @Test
  void UserController_GetMyUserInfo_Unauthenticated() {
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void UserController_GetMyUserInfo_InvalidUserId() {
    when(userService.getUserData(any())).thenReturn(Optional.empty());

    // Request
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void UserController_GetMyUserInfo_InvalidRole() {
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                new GetUserData(
                    FakeGenerator.username(), FakeGenerator.email(), Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.empty());

    // Request
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  // endregion

  // region DELETE /me Tests

  @Test
  void UserController_DeleteMyUser_Successful() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .delete()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_DeleteMyUser_Unauthenticated() {
    client
        .delete()
        .uri(UserControllerUriFactory.getMePath())
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  // endregion

  // region PATCH /me Tests

  @Test
  void UserController_UpdateMyUserInfo_Successful() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .body(Map.of("username", username, "email", email, "password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Successful_OnlyUsername() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .body(Map.of("username", FakeGenerator.username()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Successful_OnlyEmail() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .body(Map.of("email", FakeGenerator.email()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Successful_OnlyPassword() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    // Request
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .body(Map.of("password", FakeGenerator.password()))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Unauthenticated() {
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .body(Map.of("username", FakeGenerator.username()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  // endregion
}
