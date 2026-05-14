package com.familymoney.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.config.SecurityConfig;
import com.familymoney.domains.admin.controllers.UserAdminController;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.user.controllers.mappers.GetUserResponseMapper;
import com.familymoney.domains.user.controllers.mappers.UpdateUserRequestMapper;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.domains.user.services.data.UserData;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.properties.AppProperties;
import com.familymoney.properties.JwtProperties;
import com.familymoney.security.JwtUtils;
import com.familymoney.utils.AdminControllerUriFactory;
import com.familymoney.utils.FakeGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = UserAdminController.class,
    properties = {
      "spring.application.name=testapp",
      "jwt.key=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
@Import({
  JwtUtils.class,
  SecurityConfig.class,
  GetUserResponseMapper.class,
  UpdateUserRequestMapper.class
})
@EnableConfigurationProperties({AppProperties.class, JwtProperties.class})
@AutoConfigureRestTestClient
class UserAdminControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  // region Fields

  @Autowired private RestTestClient client;

  @MockitoSpyBean private JwtUtils jwtUtils;
  @MockitoBean private IUserService userService;
  @MockitoBean private io.jsonwebtoken.Clock jwtClock;
  @MockitoSpyBean private GetUserResponseMapper getUserResponseMapper;
  @MockitoSpyBean private UpdateUserRequestMapper updateUserRequestMapper;

  // endregion

  @BeforeEach
  void setup() {
    when(jwtClock.now()).thenReturn(Date.from(now));
  }

  // region GetUserInfo Tests

  @Test
  void UserAdminController_GetUserInfo_Successful() {
    val family = TokenFamily.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.fromUuid(UUID.randomUUID());
    when(userService.getUserData(any()))
        .thenReturn(
            Optional.of(
                UserData.builder()
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
            .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
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
  void UserAdminController_GetUserInfo_NonAdmin() {
    val family = TokenFamily.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val userId = UserId.fromUuid(UUID.randomUUID());
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

    client
        .get()
        .uri(AdminControllerUriFactory.getUserPath(userId))
        .header("Authorization", "Bearer " + jwtUtils.generateAccessToken(userId, family))
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void UserAdminController_GetUserInfo_Unauthenticated() {
    val userId = UserId.fromUuid(UUID.randomUUID());

    client
        .get()
        .uri(AdminControllerUriFactory.getUserPath(userId))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
