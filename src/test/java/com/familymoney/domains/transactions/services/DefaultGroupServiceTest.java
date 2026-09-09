package com.familymoney.domains.transactions.services;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.mappers.GroupDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.properties.GroupInvitationProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DefaultGroupServiceTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");
  private static final CurrencyUnit CURRENCY_USD = Monetary.getCurrency("USD");

  @Mock private GroupRepository groupRepository;
  @Mock private GroupInvitationRepository groupInvitationRepository;
  @Mock private GroupOperations groupOperations;
  @Spy private final Clock clock = Clock.fixed(NOW, DEFAULT_TIMEZONE_OFFSET);
  @Mock private GroupInvitationProperties groupInvitationProperties;

  @InjectMocks private DefaultGroupService groupService;

  private GroupEntity groupDbo(final GroupId id) {
    return new GroupEntity(
        id, GroupName.fromString("group"), Description.of("desc"), CURRENCY_USD, NOW, NOW);
  }

  private GroupInvitationEntity invitationDbo(
      GroupId groupId, GroupInvitationToken token, Instant expiresAt) {
    return new GroupInvitationEntity(
        UUID.randomUUID(), groupId, null, NOW, ExpirationTime.of(expiresAt));
  }

  private void mockAddUserToGroupRepository() {
    when(groupRepository.addUser(any(UserId.class), any(GroupId.class)))
        .thenAnswer(
            invocation -> {
              UserId userId = invocation.getArgument(0, UserId.class);
              GroupId groupId = invocation.getArgument(1, GroupId.class);
              return Optional.of(new UserGroupEntity(userId, groupId, NOW));
            });
  }

  private void mockCreateInGroupInvitationRepository() {
    when(groupInvitationRepository.create(any(CreateGroupInvitationDto.class)))
        .thenAnswer(
            invocation -> {
              CreateGroupInvitationDto dto =
                  invocation.getArgument(0, CreateGroupInvitationDto.class);
              return Optional.of(
                  new GroupInvitationEntity(
                      dto.id(), // return the same UserId received
                      dto.groupId(),
                      dto.userId(),
                      NOW,
                      dto.expiresAt()));
            });
  }

  @Nested
  class CreateGroupAndAddCreatorAsMember {

    @Test
    void returns_id_when_repository_succeeds() {
      final GroupName groupName = GroupName.fromString("mygroup");
      final Description desc = Description.of("mydesc");
      final UserId createdBy = UserId.generate();
      final GroupId groupId = GroupId.generate();
      when(groupOperations.createGroup(groupName, desc, CURRENCY_USD)).thenReturn(groupId);
      mockAddUserToGroupRepository();

      assertThatCode(
              () ->
                  groupService.createGroupAndAddCreatorAsMember(
                      groupName, desc, CURRENCY_USD, createdBy))
          .doesNotThrowAnyException();

      verify(groupRepository).addUser(createdBy, groupId);
    }

    @Test
    void throws_when_repository_returns_empty() {
      when(groupOperations.createGroup(any(), any(), any()))
          .thenThrow(new DatabaseExecutionException("Unable to create group"));

      final GroupName groupName = GroupName.fromString("n");
      final Description desc = Description.of("d");
      final UserId userId = UserId.generate();
      assertThatThrownBy(
              () ->
                  groupService.createGroupAndAddCreatorAsMember(
                      groupName, desc, CURRENCY_USD, userId))
          .isInstanceOf(DatabaseExecutionException.class)
          .hasMessageContaining("Unable to create group");
    }

    @Test
    void throws_when_addUser_returns_empty() {
      when(groupOperations.createGroup(any(), any(), any())).thenReturn(GroupId.generate());
      when(groupRepository.addUser(any(UserId.class), any(GroupId.class)))
          .thenReturn(Optional.empty());

      final GroupName groupName = GroupName.fromString("n");
      final Description desc = Description.of("d");
      final UserId userId = UserId.generate();
      assertThatThrownBy(
              () ->
                  groupService.createGroupAndAddCreatorAsMember(
                      groupName, desc, CURRENCY_USD, userId))
          .isInstanceOf(DatabaseExecutionException.class)
          .hasMessageContaining("Unable to assign owner to the new group");
    }

    @Test
    void throws_user_not_found_when_addUser_fails_due_to_user_deleted_concurrently() {
      when(groupOperations.createGroup(any(), any(), any())).thenReturn(GroupId.generate());
      when(groupRepository.addUser(any(UserId.class), any(GroupId.class)))
          .thenThrow(new DataIntegrityViolationException("FK violation"));

      final GroupName groupName = GroupName.fromString("n");
      final Description desc = Description.of("d");
      final UserId userId = UserId.generate();
      assertThatThrownBy(
              () ->
                  groupService.createGroupAndAddCreatorAsMember(
                      groupName, desc, CURRENCY_USD, userId))
          .isInstanceOf(GroupOwnerNotFoundException.class);
    }
  }

  @Nested
  class DeleteGroup {

    @Test
    void deletes_group_when_group_exists_and_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();

      groupService.deleteGroup(gid, user);

      verify(groupOperations).deleteGroup(gid);
    }

    @Test
    void throws_when_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> groupService.deleteGroup(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_when_group_exists_and_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> groupService.deleteGroup(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }

  @Nested
  class GetGroupsByUser {

    @Test
    void get_page_from_repository() {
      final UserId user = UserId.generate();
      final GroupId gid = GroupId.generate();
      final GroupEntity g = groupDbo(gid);
      final Pageable p = PageRequest.of(0, 10);
      when(groupOperations.getGroupsByUser(user, p))
          .thenReturn(new PageImpl<>(List.of(GroupDataMapper.fromDbo(g))));

      final Page<GroupData> page = groupService.getGroupsByUser(user, p);

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getContent().get(0).id()).isEqualTo(gid);
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    void returns_data_when_group_exists_and_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      when(groupOperations.getGroupInfo(gid)).thenReturn(GroupDataMapper.fromDbo(groupDbo(gid)));

      final GroupData data = groupService.getGroupInfo(gid, user);

      assertThat(data).isNotNull();
      assertThat(data.id()).isEqualTo(gid);
    }

    @Test
    void throws_when_group_exists_and_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> groupService.getGroupInfo(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> groupService.getGroupInfo(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    void calls_repository_when_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();

      final UpdateGroupData data = new UpdateGroupData(null, Description.of("new"));
      groupService.updateGroupInfo(gid, user, data);

      verify(groupOperations).updateGroupInfo(gid, data);
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      final UpdateGroupData data = new UpdateGroupData(null, Description.of("new"));
      assertThatThrownBy(() -> groupService.updateGroupInfo(gid, user, data))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      final UpdateGroupData data = new UpdateGroupData(null, Description.of("new"));
      assertThatThrownBy(() -> groupService.updateGroupInfo(gid, user, data))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class GetInvitationToken {

    @BeforeEach()
    void beforeEach() {
      lenient()
          .when(groupInvitationProperties.invitationDuration())
          .thenReturn(Duration.ofHours(1));
      lenient().when(groupInvitationProperties.maxNumInvitations()).thenReturn(5);
    }

    @Test
    void returns_token_when_created() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      mockCreateInGroupInvitationRepository();

      assertThatCode(() -> groupService.getInvitationToken(gid, user)).doesNotThrowAnyException();

      verify(groupInvitationRepository).create(any(CreateGroupInvitationDto.class));
    }

    @Test
    void throws_when_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> groupService.getInvitationToken(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_create_fails() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      when(groupInvitationRepository.create(any(CreateGroupInvitationDto.class)))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.getInvitationToken(gid, user))
          .isInstanceOf(DatabaseExecutionException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> groupService.getInvitationToken(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_maximum_number_of_invitations_reached() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      when(groupInvitationRepository.countByGroupIdAndUserId(gid, user)).thenReturn(20L);

      assertThatThrownBy(() -> groupService.getInvitationToken(gid, user))
          .isInstanceOf(MaximumGroupInvitationsReachedException.class);
    }
  }

  @Nested
  class EnterToGroupWithToken {

    @Test
    void adds_user_when_token_is_valid() {
      final GroupId gid = GroupId.generate();
      final GroupInvitationToken token = GroupInvitationToken.generate();
      final GroupInvitationEntity invitation = invitationDbo(gid, token, NOW.plusSeconds(3600));
      final UserId user = UserId.generate();
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

      groupService.enterToGroupWithToken(token, user);

      verify(groupInvitationRepository).deleteByToken(token);
      verify(groupRepository).addUser(user, gid);
    }

    @Test
    void throws_when_token_is_missing() {
      final GroupInvitationToken token = GroupInvitationToken.generate();
      final UserId user = UserId.generate();
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, user))
          .isInstanceOf(GroupInvitationInvalidException.class);
    }

    @Test
    void throws_when_token_is_expired() {
      final GroupId gid = GroupId.generate();
      final GroupInvitationToken token = GroupInvitationToken.generate();
      final GroupInvitationEntity invitation = invitationDbo(gid, token, NOW.minusSeconds(10));
      final UserId user = UserId.generate();
      when(groupInvitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));

      assertThatThrownBy(() -> groupService.enterToGroupWithToken(token, user))
          .isInstanceOf(GroupInvitationInvalidException.class)
          .hasMessageContaining("expired");
    }
  }

  @Nested
  class GetUsersInGroup {

    @Test
    void returns_list_when_group_exists_and_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      final UserId other = UserId.generate();
      when(groupOperations.getUsersInGroup(gid)).thenReturn(List.of(user, other));

      final List<UserId> users = groupService.getUsersInGroup(gid, user);

      assertThat(users).containsExactly(user, other);
    }

    @Test
    void throws_when_group_exists_but_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> groupService.getUsersInGroup(gid, user))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> groupService.getUsersInGroup(gid, user))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class RemoveUserFromGroup {

    @Test
    void calls_delete_when_group_exists_and_user_is_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      final UserId toRemove = UserId.generate();
      assertThatCode(() -> groupService.removeUserFromGroup(gid, user, toRemove))
          .doesNotThrowAnyException();

      verify(groupOperations).removeUserFromGroup(gid, toRemove);
    }

    @Test
    void throws_when_group_exists_and_user_is_not_member_of_the_group() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      final UserId toRemove = UserId.generate();
      doThrow(new UserIsNotMemberOfGroupException("User is not a member of the group"))
          .when(groupOperations)
          .checkIfUserIsInGroup(user, gid);

      assertThatThrownBy(() -> groupService.removeUserFromGroup(gid, user, toRemove))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }

    @Test
    void throws_when_group_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      final UserId toRemove = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(gid);

      assertThatThrownBy(() -> groupService.removeUserFromGroup(gid, user, toRemove))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_user_doesnt_exist() {
      final GroupId gid = GroupId.generate();
      final UserId user = UserId.generate();
      final UserId toRemove = UserId.generate();
      doThrow(new UserNotFoundException("User not found"))
          .when(groupOperations)
          .removeUserFromGroup(gid, toRemove);

      assertThatThrownBy(() -> groupService.removeUserFromGroup(gid, user, toRemove))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  class AddUserToGroupAsAdmin {

    @Test
    void adds_existing_user_to_existing_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      when(groupRepository.addUser(userId, groupId))
          .thenReturn(Optional.of(new UserGroupEntity(userId, groupId, Instant.now())));

      groupService.addUserToGroupAsAdmin(groupId, userId);

      verify(groupOperations).checkIfGroupExists(groupId);
      verify(groupOperations).checkIfUserExists(userId);
      verify(groupRepository).addUser(userId, groupId);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      doThrow(new TransactionGroupNotFoundException("Group not found"))
          .when(groupOperations)
          .checkIfGroupExists(groupId);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_user_does_not_exist() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      doThrow(new UserNotFoundException("User not found"))
          .when(groupOperations)
          .checkIfUserExists(userId);

      assertThatThrownBy(() -> groupService.addUserToGroupAsAdmin(groupId, userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }
}
