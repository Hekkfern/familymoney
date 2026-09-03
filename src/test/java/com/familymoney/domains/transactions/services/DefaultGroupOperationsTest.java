package com.familymoney.domains.transactions.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DefaultGroupOperationsTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private GroupRepository groupRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private DefaultGroupOperations groupOperations;

  private GroupEntity groupEntity(final GroupId groupId) {
    return new GroupEntity(
        groupId,
        GroupName.fromString("group"),
        Description.of("description"),
        Monetary.getCurrency("USD"),
        NOW,
        NOW);
  }

  @Nested
  class CreateGroup {

    @Test
    void creates_group_without_members() {
      when(groupRepository.create(any(CreateGroupDto.class)))
          .thenAnswer(
              invocation ->
                  Optional.of(groupEntity(invocation.getArgument(0, CreateGroupDto.class).id())));

      final GroupId groupId =
          groupOperations.createGroup(
              GroupName.fromString("group"),
              Description.of("description"),
              Monetary.getCurrency("USD"));

      assertThat(groupId).isNotNull();
      verify(groupRepository).create(any(CreateGroupDto.class));
    }

    @Test
    void throws_when_group_cannot_be_created() {
      when(groupRepository.create(any(CreateGroupDto.class))).thenReturn(Optional.empty());

      final GroupName groupName = GroupName.fromString("group");
      final Description description = Description.of("description");
      final CurrencyUnit currency = Monetary.getCurrency("USD");
      assertThatThrownBy(() -> groupOperations.createGroup(groupName, description, currency))
          .isInstanceOf(DatabaseExecutionException.class);
    }
  }

  @Nested
  class DeleteGroup {

    @Test
    void deletes_existing_group() {
      final GroupId groupId = GroupId.generate();

      groupOperations.deleteGroup(groupId);

      verify(groupRepository).deleteById(groupId);
    }
  }

  @Nested
  class CheckIfGroupExists {

    @Test
    void does_not_throw_when_group_exists() {
      final GroupId groupId = GroupId.generate();
      when(groupRepository.existsById(groupId)).thenReturn(true);

      assertThatCode(() -> groupOperations.checkIfGroupExists(groupId)).doesNotThrowAnyException();
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();
      when(groupRepository.existsById(groupId)).thenReturn(false);

      assertThatThrownBy(() -> groupOperations.checkIfGroupExists(groupId))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }
  }

  @Nested
  class GetGroupsByUser {

    @Test
    void returns_groups_from_repository() {
      final UserId userId = UserId.generate();
      final PageRequest pageable = PageRequest.of(0, 10);
      final GroupEntity group = groupEntity(GroupId.generate());
      when(groupRepository.findByUserId(userId, pageable))
          .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(group)));

      final List<GroupData> groups = groupOperations.getGroupsByUser(userId, pageable).getContent();

      assertThat(groups).hasSize(1).first().extracting(GroupData::id).isEqualTo(group.id());
    }
  }

  @Nested
  class GetGroupInfo {

    @Test
    void returns_existing_group() {
      final GroupId groupId = GroupId.generate();
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(groupEntity(groupId)));

      assertThat(groupOperations.getGroupInfo(groupId).id()).isEqualTo(groupId);
    }
  }

  @Nested
  class UpdateGroupInfo {

    @Test
    void updates_existing_group() {
      final GroupId groupId = GroupId.generate();
      final UpdateGroupData data = new UpdateGroupData(null, Description.of("updated"));

      groupOperations.updateGroupInfo(groupId, data);

      verify(groupRepository).updateById(org.mockito.ArgumentMatchers.eq(groupId), any());
    }
  }

  @Nested
  class GetUsersInGroup {

    @Test
    void returns_group_members() {
      final GroupId groupId = GroupId.generate();
      final List<UserId> users = List.of(UserId.generate());
      when(groupRepository.findUserIdsByGroupId(groupId)).thenReturn(users);

      assertThat(groupOperations.getUsersInGroup(groupId)).isEqualTo(users);
    }
  }

  @Nested
  class RemoveUserInGroup {

    @Test
    void removes_existing_user_from_existing_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();

      groupOperations.removeUserFromGroup(groupId, userId);

      verify(groupRepository).deleteUser(userId, groupId);
    }
  }

  @Nested
  class CheckIfUserExists {

    @Test
    void does_not_throw_when_user_exists() {
      final UserId userId = UserId.generate();
      when(userRepository.existsById(userId)).thenReturn(true);

      assertThatCode(() -> groupOperations.checkIfUserExists(userId)).doesNotThrowAnyException();
    }

    @Test
    void throws_when_user_does_not_exist() {
      final UserId userId = UserId.generate();
      when(userRepository.existsById(userId)).thenReturn(false);

      assertThatThrownBy(() -> groupOperations.checkIfUserExists(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  class CheckIfUserIsInGroup {

    @Test
    void does_not_throw_when_user_is_in_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      when(groupRepository.isUserInGroup(userId, groupId)).thenReturn(true);

      assertThatCode(() -> groupOperations.checkIfUserIsInGroup(userId, groupId))
          .doesNotThrowAnyException();
    }

    @Test
    void throws_when_user_is_not_in_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      when(groupRepository.isUserInGroup(userId, groupId)).thenReturn(false);

      assertThatThrownBy(() -> groupOperations.checkIfUserIsInGroup(userId, groupId))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }
}
