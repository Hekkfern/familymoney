package com.familymoney.domains.admin.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.services.GroupOperations;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.Optional;
import javax.money.Monetary;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class DefaultTransactionGroupAdminServiceTest {

  @Mock private GroupOperations groupOperations;
  @Mock private GroupRepository groupRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private DefaultTransactionGroupAdminService transactionGroupAdminService;

  @Nested
  class SharedOperations {

    @Test
    void delegates_group_operations_without_membership_repository_access() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      final UpdateGroupData data = new UpdateGroupData(null, Description.of("updated"));

      transactionGroupAdminService.createGroup(
          GroupName.fromString("group"),
          Description.of("description"),
          Monetary.getCurrency("USD"));
      transactionGroupAdminService.deleteGroup(groupId);
      transactionGroupAdminService.getGroupsByUser(userId, PageRequest.of(0, 10));
      transactionGroupAdminService.getGroupInfo(groupId);
      transactionGroupAdminService.updateGroupInfo(groupId, data);
      transactionGroupAdminService.getUsersInGroup(groupId);
      transactionGroupAdminService.removeUserFromGroup(groupId, userId);

      verify(groupOperations).deleteGroup(groupId);
      verify(groupOperations).getGroupInfo(groupId);
      verify(groupOperations).updateGroupInfo(groupId, data);
      verify(groupOperations).getUsersInGroup(groupId);
      verify(groupOperations).removeUserFromGroup(groupId, userId);
      verifyNoInteractions(groupRepository, userRepository);
    }
  }

  @Nested
  class AddUserToGroup {

    @Test
    void adds_existing_user_to_existing_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      when(groupRepository.addUser(userId, groupId))
          .thenReturn(Optional.of(new UserGroupEntity(userId, groupId, Instant.now())));

      transactionGroupAdminService.addUserToGroup(groupId, userId);

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

      assertThatThrownBy(() -> transactionGroupAdminService.addUserToGroup(groupId, userId))
          .isInstanceOf(TransactionGroupNotFoundException.class);
    }

    @Test
    void throws_when_user_does_not_exist() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      doThrow(new UserNotFoundException("User not found"))
          .when(groupOperations)
          .checkIfUserExists(userId);

      assertThatThrownBy(() -> transactionGroupAdminService.addUserToGroup(groupId, userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }
}
