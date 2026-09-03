package com.familymoney.domains.admin.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.admin.services.TransactionGroupAdminService;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetGroupsResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GetUsersInGroupResponseDto;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.GroupAdminControllerUriFactory;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.money.Monetary;
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
    controllers = GroupAdminController.class,
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
class DefaultGroupAdminControllerTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  @Autowired private RestTestClient client;
  @MockitoBean private TransactionGroupAdminService transactionGroupAdminService;
  @MockitoBean private Clock clock;

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
    void creates_group() {
      final GroupId groupId = GroupId.generate();
      when(transactionGroupAdminService.createGroup(any(), any(), any())).thenReturn(groupId);

      final CreateGroupResponseDto response =
          client
              .post()
              .uri(GroupAdminControllerUriFactory.getGroupsPath())
              .body(
                  Map.of(
                      "name",
                      FakeGenerator.groupName(),
                      "description",
                      FakeGenerator.description(),
                      "currencyCode",
                      "USD"))
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
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(Map.of())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void bad_request_when_name_is_invalid() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name",
                  "invalid@name",
                  "description",
                  FakeGenerator.description(),
                  "currencyCode",
                  "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void bad_request_when_name_exceeds_maximum_length() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name",
                  "a".repeat(65),
                  "description",
                  FakeGenerator.description(),
                  "currencyCode",
                  "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void bad_request_when_currency_code_is_invalid() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name",
                  FakeGenerator.groupName(),
                  "description",
                  FakeGenerator.description(),
                  "currencyCode",
                  "ZZZ"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class GetGroupsOfUser {

    @Test
    void returns_groups_for_user() {
      final UserId userId = UserId.generate();
      final GroupData group = groupData(GroupId.generate());
      when(transactionGroupAdminService.getGroupsByUser(any(), any()))
          .thenReturn(new PageImpl<>(List.of(group)));

      final GetGroupsResponseDto response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getGroupsUserPath(userId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetGroupsResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.groups()).hasSize(1);
      assertThat(response.groups().getFirst().id()).isEqualTo(group.id().value());
    }

    @Test
    void bad_request_when_user_id_is_invalid() {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupsUserPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class DeleteGroup {

    @Test
    void deletes_group() {
      final GroupId groupId = GroupId.generate();

      client
          .delete()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupAdminService).deleteGroup(groupId);
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .delete()
          .uri(GroupAdminControllerUriFactory.getGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    void returns_group_information() {
      final GroupData group = groupData(GroupId.generate());
      when(transactionGroupAdminService.getGroupInfo(group.id())).thenReturn(group);

      final GetGroupResponseDto response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getGroupPath(group.id().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetGroupResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.id()).isEqualTo(group.id().value());
      assertThat(response.name()).isEqualTo(group.name().value());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    void updates_group_information() {
      final GroupId groupId = GroupId.generate();

      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .body(Map.of("description", FakeGenerator.description()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupAdminService).updateGroupInfo(any(), any());
    }

    @Test
    void bad_request_when_name_is_invalid() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("name", "invalid@name"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void updates_description_when_description_is_blank() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", "  "))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void bad_request_when_description_is_invalid() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", "a".repeat(400)))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class AddUserToGroup {

    @Test
    void adds_user_to_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();

      client
          .post()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  groupId.toString(), userId.toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupAdminService).addUserToGroup(groupId, userId);
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .post()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  "invalid", UserId.generate().toString()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void bad_request_when_user_id_is_invalid() {
      client
          .post()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  GroupId.generate().toString(), "invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class RemoveUserFromGroup {

    @Test
    void removes_user_from_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();

      client
          .delete()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  groupId.toString(), userId.toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(transactionGroupAdminService).removeUserFromGroup(groupId, userId);
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .delete()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  "invalid", UserId.generate().toString()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }

    @Test
    void bad_request_when_user_id_is_invalid() {
      client
          .delete()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  GroupId.generate().toString(), "invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }

  @Nested
  class GetUsersInGroup {

    @Test
    void returns_users_in_group() {
      final GroupId groupId = GroupId.generate();
      final List<UserId> users = List.of(UserId.generate(), UserId.generate());
      when(transactionGroupAdminService.getUsersInGroup(groupId)).thenReturn(users);

      final GetUsersInGroupResponseDto response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getUsersForGroupPath(groupId.toString()))
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
          .uri(GroupAdminControllerUriFactory.getUsersForGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(transactionGroupAdminService);
    }
  }
}
