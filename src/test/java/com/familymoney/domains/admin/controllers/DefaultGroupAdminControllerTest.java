package com.familymoney.domains.admin.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.services.GroupService;
import com.familymoney.domains.transactions.services.data.BalanceData;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.security.JwtAuthFilter;
import com.familymoney.testutils.FakeGenerator;
import com.familymoney.testutils.GroupAdminControllerUriFactory;
import com.familymoney.testutils.WithMockUserId;
import com.familymoney.utils.PageResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
@WithMockUserId(userId = DefaultGroupAdminControllerTest.USER_ID, role = "ADMIN")
class DefaultGroupAdminControllerTest {

  static final String USER_ID = "01a19afd-6fa1-74f3-a638-7db6cef7c8ee";

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
    void bad_request_when_body_is_missing() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_name_is_missing() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(Map.of("description", FakeGenerator.description(), "currencyCode", "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_description_is_missing() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(Map.of("name", FakeGenerator.groupName(), "currencyCode", "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_currency_code_is_missing() {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of("name", FakeGenerator.groupName(), "description", FakeGenerator.description()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "",
          "   ",
          "\n",
          "\t",
          "invalid@name",
          "iRaboXFhTyaCUjtNnDRruOBGvxBcXiXuqqDcvknrrHmQrASzPSALnLvLXrhrKJStx"
        })
    void bad_request_when_name_is_invalid(final String name) {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name", name, "description", FakeGenerator.description(), "currencyCode", "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "KDMKPakwYxxbBHMLOpsdCGZyAuFcImgrtXAhEqVJXECwveLTbLGfMcWzToklJVbAesDMRIhpzhlTouzxPIkqivMaAPnlaagVApVJBUwDHUhKJVSDubRgckrlNbUbGqLRvErYMQyurKGQXmzHaQKqQTbaqXoBxOsvGLfAkFbEksSBLUIWjRyEyhbYkJYkcgKOYQYhOkaVDEEKCeSGKSTPjtjsaCkRWrDZuDNEWHmUgrNgVeuSUqbzOSmmIxfPiZin"
        })
    void bad_request_when_description_is_invalid(final String description) {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name",
                  FakeGenerator.groupName(),
                  "description",
                  description,
                  "currencyCode",
                  "USD"))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\n", "\t"})
    void creates_group_when_description_is_empty_or_blank(final String description) {
      final GroupId groupId = GroupId.generate();
      when(groupService.createEmptyGroup(any(), any(), any(), any())).thenReturn(groupId);

      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupsPath())
          .body(
              Map.of(
                  "name",
                  FakeGenerator.groupName(),
                  "description",
                  description,
                  "currencyCode",
                  "USD"))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).createEmptyGroup(any(), eq(Description.of("")), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\n", "\t", "invalid", "ZZZ"})
    void bad_request_when_currency_code_is_invalid(final String currencyCode) {
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
                  currencyCode))
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
              .uri(uribuilder ->
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
    void returns_empty_when_user_has_no_groups() {
      final UserId userId = UserId.generate();
      when(groupService.getGroupsForUser(userId, 0, 20)).thenReturn(new PageImpl<>(List.of()));

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
      assertThat(response.content()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_user_id_is_blank(final String userId) {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupsUserPath(userId))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1000})
    void bad_request_when_page_is_invalid(final int page) {
      client
          .get()
          .uri(uriBuilder ->
              uriBuilder
                  .path(
                      GroupAdminControllerUriFactory.getGroupsUserPath(
                          UserId.generate().toString()))
                  .queryParam("page", page)
                  .build())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 19, 101})
    void bad_request_when_size_is_invalid(final int size) {
      client
          .get()
          .uri(uriBuilder ->
              uriBuilder
                  .path(
                      GroupAdminControllerUriFactory.getGroupsUserPath(
                          UserId.generate().toString()))
                  .queryParam("size", size)
                  .build())
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
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

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .delete()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId))
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
    void not_found_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();
      when(groupService.getGroupInfo(eq(groupId), any()))
          .thenThrow(new GroupNotFoundException("Group not found"));

      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .exchange()
          .expectStatus()
          .isNotFound();
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId))
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
    void updates_only_name_field() {
      final GroupId groupId = GroupId.generate();
      final GroupName groupName = GroupName.fromString(FakeGenerator.groupName());

      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .body(Map.of("name", groupName.value()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService)
          .updateGroupInfo(
              eq(groupId), any(), eq(UpdateGroupData.builder().name(groupName).build()));
    }

    @Test
    void updates_only_description_field() {
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

    @ParameterizedTest
    @ValueSource(
        strings = {
          "",
          "   ",
          "\n",
          "\t",
          "invalid@name",
          "iRaboXFhTyaCUjtNnDRruOBGvxBcXiXuqqDcvknrrHmQrASzPSALnLvLXrhrKJStx"
        })
    void bad_request_when_name_is_invalid(final String name) {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("name", name))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\n", "\t"})
    void updates_description_when_description_is_empty_or_blank(final String description) {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", description))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService)
          .updateGroupInfo(
              any(),
              any(),
              eq(UpdateGroupData.builder().description(Description.of(description)).build()));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "KDMKPakwYxxbBHMLOpsdCGZyAuFcImgrtXAhEqVJXECwveLTbLGfMcWzToklJVbAesDMRIhpzhlTouzxPIkqivMaAPnlaagVApVJBUwDHUhKJVSDubRgckrlNbUbGqLRvErYMQyurKGQXmzHaQKqQTbaqXoBxOsvGLfAkFbEksSBLUIWjRyEyhbYkJYkcgKOYQYhOkaVDEEKCeSGKSTPjtjsaCkRWrDZuDNEWHmUgrNgVeuSUqbzOSmmIxfPiZin"
        })
    void bad_request_when_description_is_invalid(final String description) {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
          .body(Map.of("description", description))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId))
          .body(Map.of("name", FakeGenerator.groupName()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @Test
    void bad_request_when_body_is_missing() {
      client
          .patch()
          .uri(GroupAdminControllerUriFactory.getGroupPath(GroupId.generate().toString()))
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

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .post()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  groupId, UserId.generate().toString()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_user_id_is_invalid(final String userIdToAdd) {
      client
          .post()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  GroupId.generate().toString(), userIdToAdd))
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

      verify(groupService).removeUserFromGroup(eq(groupId), eq(userId), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .delete()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  groupId, UserId.generate().toString()))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_user_id_is_invalid(final String userIdToRemove) {
      client
          .delete()
          .uri(
              GroupAdminControllerUriFactory.getGroupAndUserPath(
                  GroupId.generate().toString(), userIdToRemove))
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

      assertThat(response)
          .isNotNull()
          .containsExactlyElementsOf(
              users.stream()
                  .map(user -> new MemberDto(user.userId().value(), user.username().value()))
                  .toList());
    }

    @Test
    void returns__nothing_when_group_has_no_users() {
      final GroupId groupId = GroupId.generate();
      when(groupService.getUsersInGroup(eq(groupId), any())).thenReturn(List.of());

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

      assertThat(response).isNotNull().isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getUsersForGroupPath(groupId))
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
      final GroupId groupId = GroupId.generate();
      final BalanceData balance =
          new BalanceData(
              UserId.generate(), UserId.generate(), Money.of(new BigDecimal("12.50"), "USD"));
      when(groupService.getGroupBalances(eq(groupId), any())).thenReturn(List.of(balance));

      final List<BalanceDto> response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getGroupBalancesPath(groupId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<List<BalanceDto>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(response).hasSize(1);
      assertThat(response.getFirst().creditor()).isEqualTo(balance.creditor().value());
      assertThat(response.getFirst().debitor()).isEqualTo(balance.debitor().value());
      assertThat(response.getFirst().amount()).isEqualByComparingTo("12.50");
      assertThat(response.getFirst().currency()).isEqualTo("USD");
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupBalancesPath(groupId))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class ForceSyncBalances {

    @Test
    void forces_balances_sync() {
      final GroupId groupId = GroupId.generate();

      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId.toString()))
          .exchange()
          .expectStatus()
          .isOk();

      verify(groupService).forceSyncBalancesAsAdmin(groupId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .post()
          .uri(GroupAdminControllerUriFactory.getGroupPath(groupId))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }

  @Nested
  class GetGroupTransactions {

    @Test
    void returns_group_transactions() {
      final GroupId groupId = GroupId.generate();
      final PaymentData payment =
          new PaymentData(
              PaymentId.generate(),
              groupId,
              Description.of(FakeGenerator.description()),
              Money.of(new BigDecimal("30.00"), "USD"),
              Instant.parse("2025-01-01T00:00:00Z"),
              UserId.generate(),
              UserId.generate());
      when(groupService.getGroupTransactions(eq(groupId), any(), eq(0), eq(20)))
          .thenReturn(new PageImpl<>(List.of(new TransactionData.Payment(payment))));

      final PageResponse<TransactionDto> response =
          client
              .get()
              .uri(GroupAdminControllerUriFactory.getGroupTransactionsPath(groupId.toString()))
              .exchange()
              .expectStatus()
              .isOk()
              .expectBody(new ParameterizedTypeReference<PageResponse<TransactionDto>>() {})
              .returnResult()
              .getResponseBody();

      assertThat(response).isNotNull();
      assertThat(response.content()).hasSize(1);
      assertThat(response.content().getFirst().id()).isEqualTo(payment.id().value());
      assertThat(response.content().getFirst().description())
          .isEqualTo(payment.description().value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "\n", "\t", "invalid"})
    void bad_request_when_group_id_is_invalid(final String groupId) {
      client
          .get()
          .uri(GroupAdminControllerUriFactory.getGroupTransactionsPath(groupId))
          .exchange()
          .expectStatus()
          .isBadRequest();

      verifyNoInteractions(groupService);
    }
  }
}
