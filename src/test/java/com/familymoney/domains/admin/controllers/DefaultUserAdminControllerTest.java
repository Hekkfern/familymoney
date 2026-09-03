package com.familymoney.domains.admin.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.domains.users.controllers.dtos.GetUserResponseDto;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.UserAdminControllerUriFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class DefaultUserAdminControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");

  @Autowired private RestTestClient client;

  @MockitoBean private UserService userService;
  @MockitoBean private Clock clock;

  @BeforeEach
  void setup() {
    when(clock.instant()).thenReturn(now);
  }

  @Nested
  class GetUserInfo {

    @Test
    void success() {
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final UserId userId = UserId.fromUuid(UUID.randomUUID());
      when(userService.getUserData(any()))
          .thenReturn(Optional.of(new UserData(userId, username, email, now, true, true)));
      when(userService.getUserRole(any())).thenReturn(Optional.of(Role.ADMIN));

      final GetUserResponseDto data =
          client
              .get()
              .uri(UserAdminControllerUriFactory.getUserPath(userId))
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
