package com.familymoney.domains.transactions.services;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultGroupCheckerTest {

  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  @Mock private GroupRepository groupRepository;
  @Mock private UserRepository userRepository;
  @InjectMocks private DefaultGroupChecker groupChecker;

  @Nested
  class VerifyGroupExists {

    @Test
    void does_not_throw_when_group_exists() {
      final GroupId groupId = GroupId.generate();
      when(groupRepository.existsById(groupId)).thenReturn(true);

      assertThatCode(() -> groupChecker.verifyGroupExists(groupId)).doesNotThrowAnyException();
    }

    @Test
    void throws_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();
      when(groupRepository.existsById(groupId)).thenReturn(false);

      assertThatThrownBy(() -> groupChecker.verifyGroupExists(groupId))
          .isInstanceOf(GroupNotFoundException.class);
    }
  }

  @Nested
  class VerifyUserExists {

    @Test
    void does_not_throw_when_user_exists() {
      final UserId userId = UserId.generate();
      when(userRepository.existsById(userId)).thenReturn(true);

      assertThatCode(() -> groupChecker.verifyUserExists(userId)).doesNotThrowAnyException();
    }

    @Test
    void throws_when_user_does_not_exist() {
      final UserId userId = UserId.generate();
      when(userRepository.existsById(userId)).thenReturn(false);

      assertThatThrownBy(() -> groupChecker.verifyUserExists(userId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  class VerifyUserIsInGroup {

    @Test
    void does_not_throw_when_user_is_in_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      when(groupRepository.isUserInGroup(userId, groupId)).thenReturn(true);

      assertThatCode(() -> groupChecker.verifyUserIsInGroup(userId, groupId))
          .doesNotThrowAnyException();
    }

    @Test
    void throws_when_user_is_not_in_group() {
      final GroupId groupId = GroupId.generate();
      final UserId userId = UserId.generate();
      when(groupRepository.isUserInGroup(userId, groupId)).thenReturn(false);

      assertThatThrownBy(() -> groupChecker.verifyUserIsInGroup(userId, groupId))
          .isInstanceOf(UserIsNotMemberOfGroupException.class);
    }
  }
}
