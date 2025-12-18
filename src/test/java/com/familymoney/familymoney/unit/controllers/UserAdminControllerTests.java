package com.familymoney.familymoney.unit.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.familymoney.controllers.UserController;
import com.familymoney.familymoney.controllers.dtos.admin.GetUserResponseDto;
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
import com.familymoney.familymoney.utils.AdminControllerUriFactory;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Instant;
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
public class UserAdminControllerTests {

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

  // region GetUser Tests

  @Test
  void UserAdminController_GetMyUserInfo_Successful() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val userId = FakeGenerator.userId();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
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
    assertEquals(username, data.username());
    assertEquals(email, data.email());
  }

  @Test
  void UserAdminController_GetMyUserInfo_NonAdmin() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    val userId = FakeGenerator.userId();
    when(userService.getUserData(any()))
        .thenReturn(Optional.of(new GetUserData(username, email, Instant.now(), true, true)));
    when(userService.getUserRole(any())).thenReturn(Optional.of(Role.USER));

    client
        .get()
        .uri(AdminControllerUriFactory.getUserPath(userId))
        .header("Authorization", "Bearer " + jwtUtil.generateAccessToken(FakeGenerator.userId()))
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
