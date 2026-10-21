package com.familymoney.domains.admin.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.GroupAdminControllerUriFactory;
import com.familymoney.utils.PageResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.ParameterizedTypeReference;
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
  @MockitoBean private GroupService groupService;
  @MockitoBean private Clock clock;

  private GroupData generateGroupData(final GroupId groupId) {
    return new GroupData(
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        Description.of(FakeGenerator.description()),
        Currency.getInstance("USD"));
  }

  @Nested
  class CreateEmptyGroup {

    @Test
    void creates_group() {
      final GroupId groupId = GroupId.generate();
      when(groupService.createEmptyGroup(any(), any(), any(), any())).thenReturn(groupId);

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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetGroupsOfUser {

    @Test
    void returns_groups_for_user_with_default_pagination() {
      final UserId userId = UserId.generate();
      final GroupData group = generateGroupData(GroupId.generate());
      when(groupService.getGroupsForUser(userId, 0, 20)).thenReturn(new PageImpl<>(List.of(group)));

      final PageResponse<GroupDto> response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getGroupsUserPath(userId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<PageResponse<GroupDto>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.content()).hasSize(1);
      assertThat(response.content().getFirst().id()).isEqualTo(group.id().value());
    }

    @Test
    void returns_groups_for_user_with_custom_pagination() {
      final UserId userId = UserId.generate();
      final GroupData group = generateGroupData(GroupId.generate());
      when(groupService.getGroupsForUser(userId, 1, 27)).thenReturn(new PageImpl<>(List.of(group)));

      final PageResponse<GroupDto> response =
          client
              .get()
              .uri(
                  uribuilder ->
                      uribuilder
                          .path(GroupAdminControllerUriFactory.getGroupsUserPath(userId.toString()))
                          .queryParam("page", 1)
                          .queryParam("size", 27)
                          .build())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<PageResponse<GroupDto>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.content()).hasSize(1);
      assertThat(response.content().getFirst().id()).isEqualTo(group.id().value());
    }

    @Test
    void bad_request_when_user_id_is_invalid() {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupsUserPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_page_is_invalid() {
      // TODO
    }

    @Test
    void bad_request_when_size_is_invalid() {
      // TODO
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

      verify(groupService).deleteGroupAsAdmin(groupId);
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .delete()
          .uri(GroupAdminControllerUriFactory.getGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    void returns_group_information() {
      final GroupData group = generateGroupData(GroupId.generate());
      when(groupService.getGroupInfo(eq(group.id()), any())).thenReturn(group);

      final GroupDto response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getGroupPath(group.id().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GroupDto.class)
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

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    void updates_all_fields() {
      final GroupId groupId = GroupId.generate();
      final GroupName groupName = GroupName.fromString(FakeGenerator.groupName());
      final Description description = Description.of(FakeGenerator.description());

      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .body(Map.of("name", groupName.value(), "description", description.value()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService)
          .updateGroupInfo(
              eq(groupId),
              any(),
              eq(UpdateGroupData.builder().name(groupName).description(description).build()));
    }

    @Test
    void updates_one_single_field() {
      final GroupId groupId = GroupId.generate();
      final Description description = Description.of(FakeGenerator.description());

      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .body(Map.of("description", description.value()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService)
          .updateGroupInfo(
              eq(groupId), any(), eq(UpdateGroupData.builder().description(description).build()));
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

      verifyNoInteractions(groupService);
    }

    @Test
    void updates_description_when_description_is_blank() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", "  "))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).updateGroupInfo(any(), any(), any());
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

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath("invalid"))
          .body(Map.of("name", FakeGenerator.groupName()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_no_fields_are_provided() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
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

      verify(groupService).addUserToGroupAsAdmin(groupId, userId);
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
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

      verify(groupService).removeUserFromGroup(groupId, userId, any());
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetUsersInGroup {

    @Test
    void returns_users_in_group() {
      final GroupId groupId = GroupId.generate();
      final List<MemberData> users =
          List.of(
              new MemberData(UserId.generate(), UserName.fromString(FakeGenerator.username())),
              new MemberData(UserId.generate(), UserName.fromString(FakeGenerator.username())));
      when(groupService.getUsersInGroup(eq(groupId), any())).thenReturn(users);

      final List<MemberDto> response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getUsersForGroupPath(groupId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<List<MemberDto>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response)
          .containsExactlyElementsOf(
              users.stream()
                  .map(user -> new MemberDto(user.userId().value(), user.username().value()))
                  .toList());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getUsersForGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetGroupBalances {

    @Test
    void returns_group_balances() {
      // TODO
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      // TODO
    }
  }

  @Nested
  class ForceSyncBalances {

    @Test
    void force_synces_balances() {
      // TODO
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      // TODO
    }
  }

  @Nested
  class GetGroupTransactions {

    @Test
    void returns_group_transactions() {
      // TODO
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      // TODO
    }
  }
}
