package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import java.util.List;
import javax.money.CurrencyUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Shared persistence and domain operations for transaction groups. */
public interface GroupOperations {

  /**
   * Creates a transaction group without adding members.
   *
   * @param name Name of the group
   * @param description Description of the group
   * @param currency Default currency of the group
   * @return Identifier of the created group
   */
  GroupId createGroup(GroupName name, Description description, CurrencyUnit currency);

  /**
   * Deletes a group.
   *
   * @param groupId Identifier of the group
   */
  void deleteGroup(GroupId groupId);

  /**
   * Gets the groups to which a user belongs.
   *
   * @param userId Identifier of the user
   * @param pageable Pagination configuration
   * @return Page of groups
   */
  Page<GroupData> getGroupsByUser(UserId userId, Pageable pageable);

  /**
   * Gets information about a group.
   *
   * @param groupId Identifier of the group
   * @return Group information
   */
  GroupData getGroupInfo(GroupId groupId);

  /**
   * Updates group information.
   *
   * @param groupId Identifier of the group
   * @param data Updated group data
   */
  void updateGroupInfo(GroupId groupId, UpdateGroupData data);

  /**
   * Gets the members of a group.
   *
   * @param groupId Identifier of the group
   * @return Group member identifiers
   */
  List<UserId> getUsersInGroup(GroupId groupId);

  /**
   * Removes a user from a group.
   *
   * @param groupId Identifier of the group
   * @param userId Identifier of the user to remove
   */
  void removeUserFromGroup(GroupId groupId, UserId userId);

  /**
   * Checks if a group exists, throwing an exception if not.
   *
   * @param groupId Identifier of the group
   * @throws TransactionGroupNotFoundException if the group does not exist
   */
  void checkIfGroupExists(GroupId groupId);

  /**
   * Checks if a user exists, throwing an exception if not.
   *
   * @param userId Identifier of the user
   * @throws UserNotFoundException if the user does not exist
   */
  void checkIfUserExists(UserId userId);

  /**
   * Checks if a user is a member of a group, throwing an exception if not.
   *
   * @param userId Identifier of the user
   * @param groupId Identifier of the group
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void checkIfUserIsInGroup(UserId userId, GroupId groupId);
}
