package com.familymoney.domains.admin.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.domains.users.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.users.services.IUserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.AdminControllerUriFactory;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = UserAdminController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = com.familymoney.security.JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class UserAdminControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Autowired private RestTestClient client;

  @MockitoBean private IUserService userService;

  @Nested
  class GetUserInfo {

    @Test
    void UserAdminController_GetUserInfo_Successful() {
      val username = UserName.fromString(FakeGenerator.username());
      val email = Email.fromString(FakeGenerator.email());
      val userId = UserId.fromUuid(UUID.randomUUID());
      when(userService.getUserData(any()))
          .thenReturn(Optional.of(new UserData(userId, username, email, now, true, true)));
      when(userService.getUserRole(any())).thenReturn(Optional.of(Role.ADMIN));

      val data =
          client
              .get()
              .uri(AdminControllerUriFactory.getUserPath(userId))
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
  }
}
