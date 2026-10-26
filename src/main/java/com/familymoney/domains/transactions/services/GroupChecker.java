package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;

/** Shared persistence and domain operations for transaction groups. */
public interface GroupChecker {

  /**
   * Checks if a group exists, throwing an exception if not.
   *
   * @param groupId Identifier of the group
   * @throws GroupNotFoundException if the group does not exist
   */
  void verifyGroupExists(GroupId groupId);

  /**
   * Checks if a user exists, throwing an exception if not.
   *
   * @param userId Identifier of the user
   * @throws UserNotFoundException if the user does not exist
   */
  void verifyUserExists(UserId userId);

  /**
   * Checks if a user is a member of a group, throwing an exception if not.
   *
   * @param userId Identifier of the user
   * @param groupId Identifier of the group
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void verifyUserIsInGroup(UserId userId, GroupId groupId);
}
