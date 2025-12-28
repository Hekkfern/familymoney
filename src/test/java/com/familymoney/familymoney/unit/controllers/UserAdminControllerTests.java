package com.familymoney.familymoney.unit.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.controllers.UserAdminController;
import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
import com.familymoney.familymoney.controllers.mappers.*;
import com.familymoney.familymoney.properties.AppProperties;
import com.familymoney.familymoney.properties.JwtProperties;
import com.familymoney.familymoney.security.JwtUtil;
import com.familymoney.familymoney.services.IUserService;
import com.familymoney.familymoney.services.data.GetUserData;
import com.familymoney.familymoney.types.*;
import com.familymoney.familymoney.utils.AdminControllerUriFactory;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = UserAdminController.class,
    properties = {
      "spring.application.name=testapp",
      "jwt.key=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
@Import({JwtUtil.class, GetUserResponseMapper.class, UpdateUserRequestMapper.class})
@EnableConfigurationProperties({AppProperties.class, JwtProperties.class})
public class UserAdminControllerTests {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  // region Fields

  private RestTestClient client;

  @Autowired private MockMvc mockMvc;

  @MockitoSpyBean private JwtUtil jwtUtil;
  @MockitoBean private IUserService userService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoSpyBean private GetUserResponseMapper getUserResponseMapper;
  @MockitoSpyBean private UpdateUserRequestMapper updateUserRequestMapper;

  // endregion

  @BeforeEach
  public void setup() {
    client = RestTestClient.bindTo(mockMvc).build();
    when(jwtClock.now()).thenReturn(Date.from(now));
  }

  // region GetUser Tests

  @Test
  void UserAdminController_GetMyUserInfo_Successful() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.fromUuid(UUID.randomUUID());
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                GetUserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(now)
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.ADMIN));

    val data =
        client
            .get()
            .uri(AdminControllerUriFactory.getUserPath(userId))
            .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(userId))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(GetUserResponseDto.class)
            .returnResult()
            .getResponseBody();
    assertNotNull(data);
    assertEquals(username.value(), data.username());
    assertEquals(email.value(), data.email());
    assertEquals(now, data.createdAt());
  }

  @Test
  void UserAdminController_GetMyUserInfo_NonAdmin() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.fromUuid(UUID.randomUUID());
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                GetUserData.builder()
                    .id(userId)
                    .username(username)
                    .email(email)
                    .createdAt(Instant.now())
                    .isEmailVerified(true)
                    .isEnabled(true)
                    .build()));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .get()
        .uri(AdminControllerUriFactory.getUserPath(userId))
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(userId))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void UserAdminController_GetMyUserInfo_Unauthenticated() {
    val userId = UserId.fromUuid(UUID.randomUUID());

    client
        .get()
        .uri(AdminControllerUriFactory.getUserPath(userId))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
