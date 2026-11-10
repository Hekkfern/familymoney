package com.familymoney.domains.admin.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.users.controllers.dtos.GetUserRoleResponseDto;
import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.controllers.dtos.UserDto;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.services.data.UpdateUserData;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.UserAdminControllerUriFactory;
import com.familymoney.utils.PageResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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
          .thenReturn(Optional.of(new UserData(userId, username, email, true, true)));
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
    }

    @Test
    void not_found_when_user_does_not_exist() {
      when(userService.getUserData(any())).thenReturn(Optional.empty());

      client
          .get()
          .uri(UserAdminControllerUriFactory.getUserPath(UUID.randomUUID().toString()))
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      client
          .get()
          .uri(UserAdminControllerUriFactory.getUserPath("not-a-uuid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class GetUsersInfo {

    static Stream<Map<String, String>> invalidPaginationParameters() {
      return Stream.of(
          Map.of("page", "-1"),
          Map.of("page", "10001"),
          Map.of("size", "19"),
          Map.of("size", "101"),
          Map.of("sort", "UNKNOWN"),
          Map.of("direction", "UNKNOWN"));
    }

    private UserData newUserData() {
      return new UserData(
          UserId.fromUuid(UUID.randomUUID()),
          UserName.fromString(FakeGenerator.username()),
          Email.fromString(FakeGenerator.email()),
          true,
          true);
    }

    @Test
    void get_users_info_with_default_pagination() {
      final UserData userData = newUserData();
      when(userService.getUsers(any()))
          .thenReturn(new PageImpl<>(List.of(userData), PageRequest.of(0, 25), 1));

      final PageResponse<UserDto> response =
          client
              .get()
              .uri(UserAdminControllerUriFactory.getUsersPath(Map.of()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<PageResponse<UserDto>>() {})
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertEquals(1, response.content().size());
      assertEquals(userData.username().value(), response.content().get(0).username());
      assertEquals(0, response.page());
      assertEquals(25, response.size());
      final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
      verify(userService).getUsers(captor.capture());
      final Pageable pageable = captor.getValue();
      assertEquals(0, pageable.getPageNumber());
      assertEquals(25, pageable.getPageSize());
      assertEquals(
          Sort.Direction.DESC, pageable.getSort().getOrderFor("created_at").getDirection());
    }

    @Test
    void get_users_info_with_custom_pagination_and_sorting() {
      when(userService.getUsers(any()))
          .thenReturn(new PageImpl<>(List.of(newUserData()), PageRequest.of(2, 50), 101));

      client
          .get()
          .uri(
              UserAdminControllerUriFactory.getUsersPath(
                  Map.of("page", "2", "size", "50", "sort", "USERNAME", "direction", "ASC")))
          .exchange()
          .expectStatus()
          .isOk();

      final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
      verify(userService).getUsers(captor.capture());
      final Pageable pageable = captor.getValue();
      assertEquals(2, pageable.getPageNumber());
      assertEquals(50, pageable.getPageSize());
      assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("username").getDirection());
    }

    @ParameterizedTest
    @MethodSource("invalidPaginationParameters")
    void bad_request_when_pagination_parameter_is_invalid(final Map<String, String> queryParams) {
      client
          .get()
          .uri(UserAdminControllerUriFactory.getUsersPath(queryParams))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class EnableUser {

    @Test
    void disable_user() {
      final UUID userId = UUID.randomUUID();

      client
          .put()
          .uri(UserAdminControllerUriFactory.enableUserPath(userId.toString(), "false"))
          .exchange()
          .expectStatus()
          .isOk();

      verify(userService).enableUser(UserId.fromUuid(userId), false);
    }

    @Test
    void bad_request_when_enabled_field_is_missing() {
      client
          .put()
          .uri(UserAdminControllerUriFactory.userEnablePath(UUID.randomUUID().toString()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }

    @Test
    void not_found_when_user_does_not_exist() {
      doThrow(new UserNotFoundException("User not found"))
          .when(userService)
          .enableUser(any(), anyBoolean());

      client
          .put()
          .uri(UserAdminControllerUriFactory.enableUserPath(UUID.randomUUID().toString(), "true"))
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    @Test
    void enable_user() {
      final UUID userId = UUID.randomUUID();

      client
          .put()
          .uri(UserAdminControllerUriFactory.enableUserPath(userId.toString(), "true"))
          .exchange()
          .expectStatus()
          .isOk();

      verify(userService).enableUser(UserId.fromUuid(userId), true);
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      client
          .put()
          .uri(UserAdminControllerUriFactory.enableUserPath("not-a-uuid", "true"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }

    @Test
    void bad_request_when_enabled_field_is_invalid() {
      client
          .put()
          .uri(
              UserAdminControllerUriFactory.enableUserPath(
                  UUID.randomUUID().toString(), "not-a-boolean"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class DeleteUser {

    @Test
    void delete_user() {
      final UUID userId = UUID.randomUUID();

      client
          .delete()
          .uri(UserAdminControllerUriFactory.getUserPath(userId.toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(userService).deleteUser(UserId.fromUuid(userId));
    }

    @Test
    void not_found_when_user_does_not_exist() {
      doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(any());

      client
          .delete()
          .uri(UserAdminControllerUriFactory.getUserPath(UUID.randomUUID().toString()))
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      client
          .delete()
          .uri(UserAdminControllerUriFactory.getUserPath("not-a-uuid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class UpdateUserInfo {

    @Test
    void updates_all_information() {
      final UUID userId = UUID.randomUUID();
      final String username = FakeGenerator.username();
      final String email = FakeGenerator.email();
      final String password = FakeGenerator.password();
      final UpdateUserRequestDto request =
          UpdateUserRequestDto.builder().username(username).email(email).password(password).build();

      client
          .patch()
          .uri(UserAdminControllerUriFactory.getUserPath(userId.toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .exchange()
          .expectStatus()
          .isOk();

      final UpdateUserData expected =
          new UpdateUserData(
              UserName.fromString(username),
              Email.fromString(email),
              Password.fromString(password));
      verify(userService).updateUserInfo(UserId.fromUuid(userId), expected);
    }

    @Test
    void updates_one_single_item() {
      final UUID userId = UUID.randomUUID();
      final String username = FakeGenerator.username();
      final UpdateUserRequestDto request =
          UpdateUserRequestDto.builder().username(username).build();

      client
          .patch()
          .uri(UserAdminControllerUriFactory.getUserPath(userId.toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .exchange()
          .expectStatus()
          .isOk();

      final UpdateUserData expected = new UpdateUserData(UserName.fromString(username), null, null);
      verify(userService).updateUserInfo(UserId.fromUuid(userId), expected);
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      final UpdateUserRequestDto request =
          UpdateUserRequestDto.builder().username(FakeGenerator.username()).build();

      client
          .patch()
          .uri(UserAdminControllerUriFactory.getUserPath("not-a-uuid"))
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }

    @Test
    void bad_request_when_username_is_invalid() {
      assertBadRequest(UpdateUserRequestDto.builder().username("a b").build());
    }

    @Test
    void bad_request_when_email_is_invalid() {
      assertBadRequest(UpdateUserRequestDto.builder().email("invalid").build());
    }

    @Test
    void bad_request_when_password_is_invalid() {
      assertBadRequest(UpdateUserRequestDto.builder().password("x").build());
    }

    @Test
    void not_found_when_user_does_not_exist() {
      doThrow(new UserNotFoundException("User not found"))
          .when(userService)
          .updateUserInfo(any(), any());

      client
          .patch()
          .uri(UserAdminControllerUriFactory.getUserPath(UUID.randomUUID().toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(UpdateUserRequestDto.builder().username(FakeGenerator.username()).build())
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    private void assertBadRequest(final UpdateUserRequestDto request) {
      client
          .patch()
          .uri(UserAdminControllerUriFactory.getUserPath(UUID.randomUUID().toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }

    @Test
    void bad_request_when_no_fields_are_provided() {
      client
          .patch()
          .uri(UserAdminControllerUriFactory.getUserPath(UUID.randomUUID().toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(UpdateUserRequestDto.builder().build())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class SetUserRole {

    @Test
    void sets_user_role() {
      final UUID userId = UUID.randomUUID();

      client
          .put()
          .uri(UserAdminControllerUriFactory.userRolePath(userId.toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(Role.ADMIN)
          .exchange()
          .expectStatus()
          .isOk();

      verify(userService).setUserRole(UserId.fromUuid(userId), Role.ADMIN);
    }

    @Test
    void sets_user_role_to_user() {
      final UUID userId = UUID.randomUUID();

      client
          .put()
          .uri(UserAdminControllerUriFactory.userRolePath(userId.toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(Role.USER)
          .exchange()
          .expectStatus()
          .isOk();

      verify(userService).setUserRole(UserId.fromUuid(userId), Role.USER);
    }

    @Test
    void bad_request_when_role_is_invalid() {
      client
          .put()
          .uri(UserAdminControllerUriFactory.userRolePath(UUID.randomUUID().toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body("\"UNKNOWN\"")
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }

    @Test
    void bad_request_when_body_is_missing() {
      client
          .put()
          .uri(UserAdminControllerUriFactory.userRolePath(UUID.randomUUID().toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }

    @Test
    void not_found_when_user_does_not_exist() {
      doThrow(new UserNotFoundException("User not found"))
          .when(userService)
          .setUserRole(any(), any());

      client
          .put()
          .uri(UserAdminControllerUriFactory.userRolePath(UUID.randomUUID().toString()))
          .contentType(MediaType.APPLICATION_JSON)
          .body(Role.ADMIN)
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      client
          .put()
          .uri(UserAdminControllerUriFactory.userRolePath("not-a-uuid"))
          .contentType(MediaType.APPLICATION_JSON)
          .body(Role.ADMIN)
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }

  @Nested
  class GetUserRole {

    @Test
    void gets_user_role() {
      final UUID userId = UUID.randomUUID();
      when(userService.getUserRole(UserId.fromUuid(userId))).thenReturn(Optional.of(Role.USER));

      final GetUserRoleResponseDto response =
          client
              .get()
              .uri(UserAdminControllerUriFactory.userRolePath(userId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetUserRoleResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertNotNull(response);
      assertEquals(Role.USER, response.role());
    }

    @Test
    void not_found_when_user_does_not_exist() {
      when(userService.getUserRole(any())).thenReturn(Optional.empty());

      client
          .get()
          .uri(UserAdminControllerUriFactory.userRolePath(UUID.randomUUID().toString()))
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    @Test
    void bad_request_when_userid_field_is_invalid() {
      client
          .get()
          .uri(UserAdminControllerUriFactory.userRolePath("not-a-uuid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(userService);
    }
  }
}
