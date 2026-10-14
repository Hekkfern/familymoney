package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.UserId;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import com.familymoney.utils.AuthorizedUser;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GroupService {

  /**
   * Creates a transaction group, without users.
   *
   * @param name the group name
   * @param description the group description
   * @param currency the currency for group expenses
   * @param user the user creating the group
   * @return the identifier of the created group
   * @throws UserNotFoundException if the creating user does not exist
   */
  GroupId createEmptyGroup(GroupName name, Description description, Currency currency, AuthorizedUser user);

  /**
   * Create a new group, and adds the creating user as member of the group
   *
   * @param name Name of the group to display to users
   * @param description Description of the group
   * @param currency Default currency for the group
   * @param user The user creating the group
   * @return Identifier of the newly created group
   * @throws GroupOwnerNotFoundException if the creating user does not exist
   */
  GroupId createGroupAndAddCreatorAsMember(
      GroupName name, Description description, Currency currency, AuthorizedUser user);

  /**
   * Leaves a group where the selected user is a member. Only members of the group can leave it. If
   * this user is the last member of the group, the group will be deleted.
   *
   * @param groupId Identifier of the group to leave
   * @param user The user attempting to leave the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void leaveGroup(GroupId groupId, AuthorizedUser user);

  /**
   * Deletes a transaction group.
   * WARNING! Only use by administrators
   *
   * @param groupId the identifier of the group to delete
   */
  void deleteGroupAsAdmin(GroupId groupId, AuthorizedUser user);

  /**
   * Get a paginated list of groups the user is a member of
   *
   * @param user The user to get the groups for
   * @param pageable Pagination information
   * @return Paginated list of groups
   */
  Page<GroupData> getGroupsByUser(AuthorizedUser user, Pageable pageable);

  /**
   * Get information about a specific group where the selected user is a member. Only members of the
   * group can view it.
   *
   * @param groupId Identifier of the group to retrieve information about
   * @param user The user requesting the information
   * @return Information about the group, if found.
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  GroupData getGroupInfo(GroupId groupId, AuthorizedUser user);

  /**
   * Update one or more fields of a group's information where the selected user is a member. Only
   * members of the group can update it.
   *
   * @param groupId Identifier of the group to modify
   * @param user The user requesting the information
   * @param data Data to update. Only non-null fields will be updated
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void updateGroupInfo(GroupId groupId, AuthorizedUser user, UpdateGroupData data);

  /**
   * Generate an invitation token for a group
   *
   * @param groupId Identifier of the group to generate the token for
   * @param user The user requesting the token
   * @return Invitation token for the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  GroupInvitationToken getInvitationToken(GroupId groupId, AuthorizedUser user);

  /**
   * Enter a group using an invitation token. Once used, the token becomes invalid.
   *
   * @param groupInvitationToken Invitation token to use to enter the group
   * @param user The user entering the group
   * @throws GroupInvitationInvalidException if the invitation token is invalid
   */
  void enterToGroupWithToken(GroupInvitationToken groupInvitationToken, AuthorizedUser user);

  /**
   * Get the list of users in a group where the selected user is a member. Only members of the group
   * can view it.
   *
   * @param groupId Identifier of the group
   * @param user The user requesting the list of members
   * @return List of members of the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  List<UserData> getUsersInGroup(GroupId groupId, AuthorizedUser user);

  /**
   * Adds a user to a transaction group.
   * WARNING! Only use by administrators
   *
   * @param groupId the identifier of the group
   * @param userIdToAdd the identifier of the user to add
   */
  void addUserToGroupAsAdmin(GroupId groupId, UserId userIdToAdd);

  /**
   * Get the balance between all users in a group
   *
   * @param groupId Identifier of the group
   * @param user The user requesting the balances
   * @return Map of user identifiers to their respective balances
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  Map<UserId, Money> getAllGroupBalances(GroupId groupId, AuthorizedUser user);

  /**
   * Get the balance between all users in a group as an administrator
   *
   * @param groupId Identifier of the group
   * @return Map of user identifiers to their respective balances
   * @throws GroupNotFoundException if the group does not exist
   */
  Map<UserId, Money> getAllGroupBalancesAsAdmin(GroupId groupId);

  /**
   * Get a paginated list of individual expenses in a group, ordered by most recent first
   *
   * @param groupId Identifier of the group
   * @param user The user requesting the balances
   * @param pageable Pagination information
   * @return Paginated list of expenses
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  Page<TransactionData> getGroupTransactions(GroupId groupId, AuthorizedUser user, Pageable pageable);
}
