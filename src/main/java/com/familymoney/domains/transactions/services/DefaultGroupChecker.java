package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.types.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Default implementation of shares transaction group operations. */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupChecker implements GroupChecker {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  @Override
  public void verifyGroupExists(final GroupId groupId) {
    final boolean exists = groupRepository.existsById(groupId);
    if (!exists) {
      final String msg = "Group '%s' does not exist".formatted(groupId);
      log.info(msg);
      throw new GroupNotFoundException(msg);
    }
  }

  @Override
  public void verifyUserExists(final UserId userId) {
    if (userRepository.existsById(userId)) {
      return;
    }
    final String message = "User '%s' does not exist".formatted(userId);
    log.info(message);
    throw new UserNotFoundException(message);
  }

  @Override
  public void verifyUserIsInGroup(final UserId userId, final GroupId groupId)
      throws UserIsNotMemberOfGroupException {
    if (!groupRepository.isUserInGroup(userId, groupId)) {
      final String msg = "User '%s' is not a member of group '%s'".formatted(userId, groupId);
      log.info(msg);
      throw new UserIsNotMemberOfGroupException(msg);
    }
  }
}
