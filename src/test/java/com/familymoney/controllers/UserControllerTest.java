package com.familymoney.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.familymoney.domains.user.controllers.UserController;
import com.familymoney.domains.user.controllers.dtos.GetMyUserResponseDto;
import com.familymoney.domains.user.services.IUserService;
import com.familymoney.domains.user.services.data.UserData;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.UserControllerUriFactory;
import com.familymoney.testutils.WithMockUserId;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
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
    controllers = UserController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = com.familymoney.security.JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserControllerTest {

  private final Instant now = Instant.parse("2025-01-01T00:00:00Z");
  private static final String USER_ID = "019d52d0-d1b8-7d2d-ba2d-39007c0dda4f";

  @Autowired private RestTestClient client;

  @MockitoBean private IUserService userService;

  @Nested
  class GetMyUserInfo {

    @Test
    @WithMockUserId(userId = USER_ID)
    void success() {
      val username = UserName.fromString(FakeGenerator.username());
      val email = Email.fromString(FakeGenerator.email());
      when(userService.getUserData(any()))
          .thenReturn(Optional.of(new UserData(null, username, email, now, true, true)));

      val data =
          client
              .get()
              .uri(UserControllerUriFactory.getMePath())
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
    @WithMockUserId(userId = USER_ID)
    void servererror_when_userid_cannot_be_found() {
      when(userService.getUserData(any())).thenReturn(Optional.empty());

      // Request
      client
          .get()
          .uri(UserControllerUriFactory.getMePath())
          .exchange()
          .expectStatus()
          .is5xxServerError();
    }
  }

  @Nested
  class DeleteMyUser {

    @Test
    @WithMockUserId(userId = USER_ID)
    void success() {
      client.delete().uri(UserControllerUriFactory.getMePath()).exchange().expectStatus().isOk();
    }
  }

  @Nested
  class UpdateMyUserInfo {

    @Test
    @WithMockUserId(userId = USER_ID)
    void success_when_updating_everything() {
      val newUsername = FakeGenerator.username();
      val newEmail = FakeGenerator.email();
      val newPassword = FakeGenerator.password();
      client
          .patch()
          .uri(UserControllerUriFactory.getMePath())
          .body(Map.of("username", newUsername, "email", newEmail, "password", newPassword))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void success_when_updating_username_only() {
      val newUsername = FakeGenerator.username();
      client
          .patch()
          .uri(UserControllerUriFactory.getMePath())
          .body(Map.of("username", newUsername))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void success_when_updating_email_only() {
      val newEmail = FakeGenerator.email();
      client
          .patch()
          .uri(UserControllerUriFactory.getMePath())
          .body(Map.of("email", newEmail))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void success_when_updating_password_only() {
      val newPassword = FakeGenerator.password();
      client
          .patch()
          .uri(UserControllerUriFactory.getMePath())
          .body(Map.of("password", newPassword))
          .exchange()
          .expectStatus()
          .isOk();
    }
  }
}
