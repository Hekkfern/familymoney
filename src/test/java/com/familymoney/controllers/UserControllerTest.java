package com.familymoney.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.controllers.UserController;
import com.familymoney.domains.user.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.user.controllers.mappers.GetMyUserResponseMapper;
import com.familymoney.domains.user.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.domains.user.services.data.UserData;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.security.JwtUtils;
import com.familymoney.utils.FakeGenerator;
import com.familymoney.utils.UserControllerUriFactory;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class UserControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");
  private static final String USER_ID = "019d52d0-d1b8-7d2d-ba2d-39007c0dda4f";

  // region Fields

  @Autowired private RestTestClient client;

  @MockitoSpyBean private JwtUtils jwtUtils;
  @MockitoBean private IUserService userService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoSpyBean private GetMyUserResponseMapper getMyUserResponseMapper;
  @MockitoSpyBean private UpdateUserRequestMapper updateUserRequestMapper;

  // endregion

  @BeforeEach
  void setup() {
    when(jwtClock.now()).thenReturn(Date.from(now));
  }

  // region GET /me Tests

  @Test
  @WithMockUser(username = USER_ID)
  void UserController_GetMyUserInfo_Successful() {
    val userId = UserId.fromString(USER_ID);
    var family = TokenFamily.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .username(username)
                    .email(email)
                    .createdAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    val data =
        client
            .get()
            .uri(UserControllerUriFactory.getMePath())
            .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GetMyUserResponseDto.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(data);
    assertEquals(username.value(), data.username());
    assertEquals(email.value(), data.email());
    assertEquals(now, data.createdAt());
  }

  @Test
  void UserController_GetMyUserInfo_InvalidUserId() {
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any())).thenReturn(Optional.empty());

    // Request
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  void UserController_GetMyUserInfo_InvalidRole() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.empty());

    // Request
    client
        .get()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  // endregion

  // region DELETE /me Tests

  @Test
  void UserController_DeleteMyUser_Successful() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .id(UserId.fromUuid(UUID.randomUUID()))
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .delete()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
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
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    val newUsername = FakeGenerator.username();
    val newEmail = FakeGenerator.email();
    val newPassword = FakeGenerator.password();
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .body(Map.of("username", newUsername, "email", newEmail, "password", newPassword))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Successful_OnlyUsername() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    val newUsername = FakeGenerator.username();
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .body(Map.of("username", newUsername))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Successful_OnlyEmail() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    val newEmail = FakeGenerator.email();
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .body(Map.of("email", newEmail))
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void UserController_UpdateMyUserInfo_Successful_OnlyPassword() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.generate();
    val family = TokenFamily.generate();
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    val newPassword = FakeGenerator.password();
    client
        .patch()
        .uri(UserControllerUriFactory.getMePath())
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .body(Map.of("password", newPassword))
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
