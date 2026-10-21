package com.familymoney.domains.transactions.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.InvitationTokenDto;
import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.services.UserService;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.GroupControllerUriFactory;
import com.familymoney.testutils.WithMockUserId;
import com.familymoney.utils.PageResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
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
  private static final String IDEMPOTENCY_KEY = "019d52d0-d1b8-7d2d-ba2d-39007c0dda4e";
  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final String VALID_TOKEN = "a".repeat(64);

  @Autowired private RestTestClient client;
  @MockitoBean private GroupService groupService;
  @MockitoBean private UserService userService;
  @MockitoBean private Clock clock;

  @BeforeEach
  void setup() {
    when(clock.instant()).thenReturn(NOW);
  }

  private GroupData generateGroupData(final GroupId groupId) {
    return new GroupData(
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        Description.of(FakeGenerator.description()),
        Currency.getInstance("USD"));
  }

  @Nested
  class CreateGroupAndAddCreatorAsMember {

    @Test
    @WithMockUserId(userId = USER_ID)
    void creates_group() {
      final GroupId groupId = GroupId.generate();
      when(groupService.createGroupAndAddCreatorAsMember(any(), any(), any(), any()))
          .thenReturn(groupId);
      executeIdempotentAction();

      final CreateGroupResponseDto response =
          client
              .post()
              .uri(GroupControllerUriFactory.getGroupsPath())
              .header("Idempotency-Key", IDEMPOTENCY_KEY)
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
      verify(groupService).createGroupAndAddCreatorAsMember(any(), any(), any(), any());
    }

    @Test
    void bad_request_when_required_fields_are_missing() {
      client
          .post()
          .uri(GroupControllerUriFactory.getGroupsPath())
          .header("Idempotency-Key", IDEMPOTENCY_KEY)
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
          .uri(GroupControllerUriFactory.getGroupsPath())
          .header("Idempotency-Key", IDEMPOTENCY_KEY)
          .body(
              Map.of(
                  "name", "invalid@name",
                  "description", FakeGenerator.description(),
                  "currencyCode", "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetGroupsOfUser {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_groups_for_user_with_default_pagination() {
      final GroupData group = generateGroupData(GroupId.generate());
      when(groupService.getGroupsForUser(any(), any(), any()))
          .thenReturn(new PageImpl<>(List.of(group)));

      final PageResponse<GroupDto> response =
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
      assertThat(response.groups()).extracting(GroupDto::id).containsExactly(group.id().value());
    }

    @Test
    void returns_groups_for_user_with_custom_pagination() {
      final GroupData group = generateGroupData(GroupId.generate());
      when(groupService.getGroupsForUser(any(), any(), any()))
          .thenReturn(new PageImpl<>(List.of(group)));

      final PageResponse<GroupDto> response =
          client
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path(GroupControllerUriFactory.getGroupsPath())
                          .queryParam("page", 1)
                          .queryParam("size", 13)
                          .build())
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GetGroupsResponseDto.class)
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.groups()).extracting(GroupDto::id).containsExactly(group.id().value());
    }

    @Test
    void bad_request_when_page_is_invalid() {
      client
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path(GroupControllerUriFactory.getGroupsPath())
                      .queryParam("page", -1)
                      .build())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_size_is_invalid() {
      client
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path(GroupControllerUriFactory.getGroupsPath())
                      .queryParam("size", 101)
                      .build())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class LeaveGroup {

    @Test
    @WithMockUserId(userId = USER_ID)
    void leaves_group() {
      client
          .delete()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).deleteGroupAsAdmin(any(), any());
    }

    @Test
    void bad_request_when_group_id_is_invalid() {
      client
          .delete()
          .uri(GroupControllerUriFactory.getGroupPath("invalid"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void unauthorized_when_service_rejects_user() {
      doThrow(new UserIsNotMemberOfGroupException("not a member"))
          .when(groupService)
          .leaveGroup(any(), any());

      client
          .delete()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isUnauthorized();
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

      verify(groupService).removeUserFromGroup(any(), any(), any());
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_group_information() {
      final GroupData group = generateGroupData(GroupId.generate());
      when(groupService.getGroupInfo(any(), any())).thenReturn(group);

      final GroupDto response =
          client
              .get()
              .uri(GroupControllerUriFactory.getGroupPath(group.id().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(GroupDto.class)
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

      verifyNoInteractions(groupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void unauthorized_when_service_rejects_user() {
      doThrow(new UserIsNotMemberOfGroupException("not a member"))
          .when(groupService)
          .getGroupInfo(any(), any());

      client
          .get()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    @WithMockUserId(userId = USER_ID)
    void updates_all_fields() {
      client
          .patch()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(
              Map.of("name", FakeGenerator.groupName(), "description", FakeGenerator.description()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).updateGroupInfo(any(), any(), any());
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void updates_one_single_field() {
      client
          .patch()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("name", FakeGenerator.groupName()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).updateGroupInfo(any(), any(), any());
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
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

      verifyNoInteractions(groupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void updates_description_when_description_is_blank() {
      client
          .patch()
          .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", "  "))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).updateGroupInfo(any(), any(), any());
    }

    @Test
    void bad_request_when_no_fields_are_provided() {
      client
        .patch()
        .uri(GroupControllerUriFactory.getGroupPath(GroupId.generate().toString()))
        .body(Map.of())
        .exchange()
        .expectStatus()
        .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetInvitationToken {

    @Test
    @WithMockUserId(userId = USER_ID)
    void returns_invitation_token() {
      when(groupService.getInvitationToken(any(), any()))
          .thenReturn(GroupInvitationToken.fromString(VALID_TOKEN));

      final InvitationTokenDto response =
          client
              .get()
              .uri(GroupControllerUriFactory.getGetInvitationPath(GroupId.generate().toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(InvitationTokenDto.class)
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

      verifyNoInteractions(groupService);
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

      verify(groupService).enterToGroupWithToken(any(), any());
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

      verifyNoInteractions(groupService);
    }

    @Test
    @WithMockUserId(userId = USER_ID)
    void not_found_when_service_rejects_token() {
      doThrow(new GroupInvitationInvalidException("expired"))
          .when(groupService)
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
      when(groupService.getUsersInGroup(any(), any())).thenReturn(users);

      final List<MemberDto> response =
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

      verifyNoInteractions(groupService);
    }
  }
}
