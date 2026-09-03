package com.familymoney.domains.transactions.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetInvitationTokenResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.services.TransactionGroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.GroupControllerUriFactory;
import com.familymoney.testutils.WithMockUserId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(
    controllers = GroupController.class,
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class DefaultGroupControllerTest {

  private static final String USER_ID = "019d52d0-d1b8-7d2d-ba2d-39007c0dda4f";
  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final String VALID_TOKEN = "a".repeat(64);

  @Autowired private RestTestClient client;
  @MockitoBean private TransactionGroupService transactionGroupService;
  @MockitoBean private UserService userService;
  @MockitoBean private Clock clock;

  @BeforeEach
  void setup() {
    when(clock.instant()).thenReturn(NOW);
  }

  private GroupData groupData(final GroupId groupId) {
    return new GroupData(
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        Description.of(FakeGenerator.description()),
        Monetary.getCurrency("USD"),
        NOW);
  }

  @Nested
  class CreateGroup {

    @Test
    @WithMockUserId(userId = USER_ID)
    void creates_group() {
      final GroupId groupId = GroupId.generate();
      when(transactionGroupService.createGroup(any(), any(), any(), any())).thenReturn(groupId);

      final CreateGroupResponseDto response =
          client
              .post()
              .uri(GroupControllerUriFactory.getGroupsPath())
              .body(
                  Map.of(
                      "name", FakeGenerator.groupName(),
                      "description", FakeGenerator.description(),
                      "currencyCode", "USD"))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(CreateGroupResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(groupId.value());
    }

    @Test
    void bad_request_when_required_fields_are_missing() {
      client
          .post()
          .uri(GroupControllerUriFactory.getGroupsPath())
          .body(Map.of())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    void bad_request_when_name_is_invalid() {
      client
          .post()
          .uri(GroupControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name", "invalid@name",
                  "description", FakeGenerator.description(),
                  "currencyCode", "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void internal_server_error_when_service_fails() {
      when(transactionGroupService.createGroup(any(), any(), any(), any()))
          .thenThrow(new DatabaseExecutionException("database unavailable"));

      client
          .post()
          .uri(GroupControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name", FakeGenerator.groupName(),
                  "description", FakeGenerator.description(),
                  "currencyCode", "USD"))
          .exchange()
          .expectStatus()
          .is5xxServerError();
    }
  }

  @Nested
  class GetGroupsOfUser {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_groups_for_user() {
      final GroupData group = groupData(GroupId.generate());
      when(transactionGroupService.getGroupsByUser(any(), any()))
          .thenReturn(new PageImpl<>(List.of(group)));

      final GetGroupsResponseDto response =
          client
              .get()
              .uri(GroupControllerUriFactory.getGroupsPath())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetGroupsResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.groups())
          .extracting(GetGroupResponseDto::id)
          .containsExactly(group.id().value());
    }
  }

  @Nested
  class DeleteGroup {

    @Test
    @WithMockUserId(userId = USER_ID)
    void deletes_group() {
      client
          .delete()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupService).deleteGroup(any(), any());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .delete()
          .uri(GroupControllerUriFactory.getGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void unauthorized_when_delete_service_rejects_user() {
      doThrow(new UserIsNotMemberOfGroupException("not a member"))
          .when(transactionGroupService)
          .deleteGroup(any(), any());

      client
          .delete()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_group_information() {
      final GroupData group = groupData(GroupId.generate());
      when(transactionGroupService.getGroupInfo(any(), any())).thenReturn(group);

      final GetGroupResponseDto response =
          client
              .get()
              .uri(GroupControllerUriFactory.getGroupPath(group.id().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetGroupResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(group.id().value());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .get()
          .uri(GroupControllerUriFactory.getGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    @WithMockUserId(userId = USER_ID)
    void updates_group_information() {
      client
          .patch()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", "updated"))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupService).updateGroupInfo(any(), any(), any());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .patch()
          .uri(GroupControllerUriFactory.getGroupPath("invalid"))
          .body(Map.of("description", "updated"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    void bad_request_when_group_name_is_invalid() {
      client
        .patch()
        .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
        .body(Map.of("name", "a".repeat(100)))
        .exchange()
        .expectStatus()
        .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    void bad_request_when_description_is_invalid() {
      client
          .patch()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", "a".repeat(400)))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }
  }

  @Nested
  class GetInvitationToken {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_invitation_token() {
      when(transactionGroupService.getInvitationToken(any(), any()))
          .thenReturn(GroupInvitationToken.fromString(VALID_TOKEN));

      final GetInvitationTokenResponseDto response =
          client
              .get()
              .uri(GroupControllerUriFactory.getGetInvitationPath(GroupId.generate().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetInvitationTokenResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.token()).isEqualTo(VALID_TOKEN);
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .get()
          .uri(GroupControllerUriFactory.getGetInvitationPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }
  }

  @Nested
  class EnterToGroup {

    @Test
    @WithMockUserId(userId = USER_ID)
    void enters_group_with_valid_token() {
      client
          .post()
          .uri(GroupControllerUriFactory.getUseInvitationPath())
          .body(Map.of("token", VALID_TOKEN))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupService).enterToGroupWithToken(any(), any());
    }

    @Test
    void bad_request_when_invitation_token_is_invalid() {
      client
          .post()
          .uri(GroupControllerUriFactory.getUseInvitationPath())
          .body(Map.of("token", "invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void not_found_when_service_rejects_token() {
      doThrow(new GroupInvitationInvalidException("expired"))
          .when(transactionGroupService)
          .enterToGroupWithToken(any(), any());

      client
          .post()
          .uri(GroupControllerUriFactory.getUseInvitationPath())
          .body(Map.of("token", VALID_TOKEN))
          .exchange()
          .expectStatus()
          .isNotFound();
    }
  }

  @Nested
  class GetUsersInGroup {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_users_in_group() {
      final List<UserId> users = List.of(UserId.generate(), UserId.generate());
      when(transactionGroupService.getUsersInGroup(any(), any())).thenReturn(users);

      final GetUsersInGroupResponseDto response =
          client
              .get()
              .uri(GroupControllerUriFactory.getUsersForGroupPath(GroupId.generate().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetUsersInGroupResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.userIds())
          .containsExactlyElementsOf(users.stream().map(UserId::value).toList());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .get()
          .uri(GroupControllerUriFactory.getUsersForGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }
  }

  @Nested
  class RemoveUserFromGroup {

    @Test
    @WithMockUserId(userId = USER_ID)
    void removes_user_from_group() {
      client
          .delete()
          .uri(
              GroupControllerUriFactory.getGroupAndUserPath(
                  GroupId.generate().toString(), UserId.generate().toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupService).removeUserFromGroup(any(), any(), any());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .delete()
          .uri(
              GroupControllerUriFactory.getGroupAndUserPath(
                  "invalid", UserId.generate().toString()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }

    @Test
    void bad_request_when_user_id_is_invalid() {
      client
          .delete()
          .uri(
              GroupControllerUriFactory.getGroupAndUserPath(
                  GroupId.generate().toString(), "invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }
  }

  @Nested
  class GroupBalances {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_group_balances() {
      when(transactionGroupService.getAllGroupBalances(any(), any()))
          .thenReturn(
              Map.of(UserId.generate(), Money.of(BigDecimal.TEN, Monetary.getCurrency("USD"))));

      client
          .get()
          .uri(GroupControllerUriFactory.getBalancesPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isOk();
    }

    @Test
    void bad_request_when_group_id_is_invalid_for_balances() {
      client
          .get()
          .uri(GroupControllerUriFactory.getBalancesPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupService);
    }
  }
}
