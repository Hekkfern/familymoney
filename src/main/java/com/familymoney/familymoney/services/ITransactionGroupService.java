package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.GroupInvitationInvalidException;
import com.familymoney.familymoney.exceptions.TransactionGroupNotFoundException;
import com.familymoney.familymoney.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.familymoney.services.data.GroupData;
import com.familymoney.familymoney.services.data.TransactionData;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.types.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.money.CurrencyUnit;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITransactionGroupService {

  /**
   * Create a new group, and adds the creating user as member of the group
   *
   * @param name Name of the group to display to users
   * @param description Description of the group
   * @param currency Default currency for the group
   * @param createdBy Identifier of the user creating the group
   * @return Identifier of the newly created group
   * @throws DatabaseExecutionException if any database operation fails
   */
  GroupId createGroup(GroupName name, String description, CurrencyUnit currency, UserId createdBy);

  /**
   * Delete a group
   *
   * @param groupId Identifier of the group to delete
   * @param userId Identifier of the user attempting to delete the group
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void deleteGroup(GroupId groupId, UserId userId);

  /**
   * Get a paginated list of groups the user is a member of
   *
   * @param userId Identifier of the user
   * @param pageable Pagination information
   * @return Paginated list of groups
   */
  Page<GroupData> getGroupsByUser(UserId userId, Pageable pageable);

  /**
   * Get information about a specific group
   *
   * @param groupId Identifier of the group to retrieve information about
   * @param user Identifier of the user requesting the information
   * @return Information about the group, if found.
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  GroupData getGroupInfo(GroupId groupId, UserId user);

  /**
   * Update one or more fields of a group's information.
   *
   * @param groupId Identifier of the group to modify
   * @param userId Identifier of the user requesting the information
   * @param data Data to update. Only non-null fields will be updated
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void updateGroupInfo(GroupId groupId, UserId userId, UpdateGroupData data);

  /**
   * Generate an invitation token for a group
   *
   * @param groupId Identifier of the group to generate the token for
   * @param userId Identifier of the user requesting the token
   * @return Invitation token for the group
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  GroupInvitationToken getInvitationToken(GroupId groupId, UserId userId);

  /**
   * Enter a group using an invitation token. Once used, the token becomes invalid.
   *
   * @param groupInvitationToken Invitation token to use to enter the group
   * @param userId Identifier of the user entering the group
   * @throws GroupInvitationInvalidException if the invitation token is invalid
   */
  void enterToGroupWithToken(GroupInvitationToken groupInvitationToken, UserId userId);

  /**
   * Get the list of users in a group
   *
   * @param groupId Identifier of the group
   * @param userId Identifier of the user requesting the list
   * @return List of members of the group
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  List<UserId> getUsersInGroup(GroupId groupId, UserId userId);

  /**
   * Remove a user from a group
   *
   * @param groupId Identifier of the group
   * @param userId Identifier of the user requesting the removal
   * @param userIdToRemove Identifier of the user to be removed from the group
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void removeUserFromGroup(GroupId groupId, UserId userId, UserId userIdToRemove);

  Map<UserId, Money> getAllGroupBalances(GroupId groupId, UserId userId);

  Page<TransactionData> getGroupTransactions(GroupId groupId, UserId userId, Pageable pageable);

  void createTransactionInGroup(
      GroupId groupId,
      String description,
      UserId from,
      UserId to,
      Money amount,
      Instant doneAt,
      UserId createdBy);

  void updateTransaction(UserId userId, TransactionId transactionId, UpdateTransactionData data);

  void deleteTransaction(UserId userId, TransactionId transactionId);
}
