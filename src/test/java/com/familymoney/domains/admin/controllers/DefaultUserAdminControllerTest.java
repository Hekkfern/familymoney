package com.familymoney.domains.admin.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.domains.users.controllers.dtos.UserDto;
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
    void get_user_info_with_default_pagination() {
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final UserId userId = UserId.fromUuid(UUID.randomUUID());
      when(userService.getUserData(any()))
          .thenReturn(Optional.of(new UserData(userId, username, email, now, true, true)));
      when(userService.getUserRole(any())).thenReturn(Optional.of(Role.ADMIN));

      final UserDto data =
          client
              .get()
              .uri(UserAdminControllerUriFactory.getUserPath(userId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(UserDto.class)
              .returnResult()
              .getResponseBody();
      assertNotNull(data);
      assertEquals(username.value(), data.username());
      assertEquals(email.value(), data.email());
      assertEquals(now, data.createdAt());
    }
  }

  @Nested
  class EnableUser {

    @Test
    void enable_user() {
      // TODO
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      // TODO
    }

    @Test
    void bad_request_when_enabled_field_is_invalid() {
      // TODO
    }
  }

  @Nested
  class DeleteUser {

    @Test
    void delete_user() {
      // TODO
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      // TODO
    }
  }

  @Nested
  class UpdateUserInfo {

    @Test
    void updates_all_information() {
      // TODO
    }

    @Test
    void updates_one_single_item() {
      // TODO
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      // TODO
    }

    @Test
    void bad_request_when_no_fields_are_provided() {}
  }

  @Nested
  class SetUserRole {

    // TODO
  }

  @Nested
  class GetUserRole {

    // TODO
  }
}
