package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupMembersReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupsPerUserReachedException;
import com.familymoney.domains.transactions.exceptions.SendGroupInvitationTooSoonException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInGroupException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInvitedException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.exceptions.WrongUserForInvitationException;
import com.familymoney.domains.transactions.services.data.BalanceData;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.util.Currency;
import java.util.List;
import org.springframework.data.domain.Page;

/** Manages transaction groups, their members, invitations, balances and transactions. */
public interface GroupService {

  /**
   * Creates a transaction group without any member.
   *
   * <p>WARNING! Only use by administrators.
   *
   * @param name the name of the group
   * @param description the description of the group
   * @param currency the default currency for the group expenses
   * @param requester the user creating the group
   * @return the identifier of the created group
   * @throws UserNotFoundException if the requester does not exist
   */
  GroupId createEmptyGroup(
      GroupName name, Description description, Currency currency, AuthorizedUser requester);

  /**
   * Creates a new group and adds the creating user as a member of it.
   *
   * @param name the name of the group
   * @param description the description of the group
   * @param currency the default currency for the group expenses
   * @param requester the user creating the group
   * @return the identifier of the created group
   * @throws UserNotFoundException if the requester does not exist
   */
  GroupId createGroupAndAddCreatorAsMember(
      GroupName name, Description description, Currency currency, AuthorizedUser requester);

  /**
   * Makes the requester leave a group. If the requester is the last member, the group is deleted.
   *
   * @param groupId the identifier of the group to leave
   * @param requester the user leaving the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the requester is not a member of the group
   */
  void leaveGroup(GroupId groupId, AuthorizedUser requester);

  /**
   * Deletes a transaction group.
   *
   * <p>WARNING! Only use by administrators.
   *
   * @param groupId the identifier of the group to delete
   * @throws GroupNotFoundException if the group does not exist
   */
  void deleteGroupAsAdmin(GroupId groupId);

  /**
   * Retrieves a paginated list of the groups a user is a member of.
   *
   * @param userId the identifier of the user whose groups are retrieved
   * @param page the zero-based page index
   * @param size the maximum number of groups per page
   * @return a page of groups
   * @throws UserNotFoundException if the user does not exist
   */
  Page<GroupData> getGroupsForUser(UserId userId, int page, int size);

  /**
   * Retrieves the information of a group. Requesters with the user role must be members of the
   * group.
   *
   * @param groupId the identifier of the group
   * @param requester the user requesting the information
   * @return the information of the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   */
  GroupData getGroupInfo(GroupId groupId, AuthorizedUser requester);

  /**
   * Updates one or more fields of a group. Requesters with the user role must be members of the
   * group.
   *
   * @param groupId the identifier of the group to modify
   * @param requester the user requesting the update
   * @param data the data to update; only the fields that are present are updated
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   */
  void updateGroupInfo(GroupId groupId, AuthorizedUser requester, UpdateGroupData data);

  /**
   * Generates an invitation token for a user to join a group and sends it to that user. Requesters
   * with the user role must be members of the group.
   *
   * @param groupId the identifier of the group the user is invited to
   * @param userId the identifier of the user to invite
   * @param requester the user sending the invitation
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserNotFoundException if the invited user does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   * @throws UserAlreadyInGroupException if the invited user is already a member of the group
   * @throws MaximumGroupMembersReachedException if the group has reached its maximum number of
   *     members
   * @throws MaximumGroupsPerUserReachedException if the invited user has reached the maximum number
   *     of groups
   * @throws MaximumGroupInvitationsReachedException if the requester has reached the maximum number
   *     of invitations
   * @throws UserAlreadyInvitedException if the user already has a pending invitation to the group
   * @throws SendGroupInvitationTooSoonException if the invitation is resent before the required
   *     waiting time has elapsed
   */
  void sendInvitationToken(GroupId groupId, UserId userId, AuthorizedUser requester);

  /**
   * Joins a group using an invitation token. Once used, the token becomes invalid.
   *
   * @param groupInvitationToken the invitation token
   * @param requester the user joining the group
   * @throws GroupInvitationInvalidException if the invitation token does not exist or has expired
   * @throws WrongUserForInvitationException if the invitation was issued for another user
   * @throws GroupNotFoundException if the group of the invitation does not exist
   * @throws MaximumGroupMembersReachedException if the group has reached its maximum number of
   *     members
   * @throws MaximumGroupsPerUserReachedException if the requester has reached the maximum number of
   *     groups
   */
  void enterToGroupWithToken(GroupInvitationToken groupInvitationToken, AuthorizedUser requester);

  /**
   * Retrieves the members of a group. Requesters with the user role must be members of the group.
   *
   * @param groupId the identifier of the group
   * @param requester the user requesting the members
   * @return the members of the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   */
  List<MemberData> getUsersInGroup(GroupId groupId, AuthorizedUser requester);

  /**
   * Adds a user to a transaction group.
   *
   * <p>WARNING! Only use by administrators.
   *
   * @param groupId the identifier of the group
   * @param userIdToAdd the identifier of the user to add
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserNotFoundException if the user does not exist
   * @throws UserAlreadyInGroupException if the user is already a member of the group
   * @throws MaximumGroupMembersReachedException if the group has reached its maximum number of
   *     members
   * @throws MaximumGroupsPerUserReachedException if the user has reached the maximum number of
   *     groups
   */
  void addUserToGroupAsAdmin(GroupId groupId, UserId userIdToAdd);

  /**
   * Removes a user from a transaction group. Requesters with the user role must be members of the
   * group.
   *
   * @param groupId the identifier of the group
   * @param userIdToRemove the identifier of the user to remove
   * @param requester the user requesting the removal
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserNotFoundException if the user to remove does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   */
  void removeUserFromGroup(GroupId groupId, UserId userIdToRemove, AuthorizedUser requester);

  /**
   * Retrieves the balances between the members of a group. Requesters with the user role must be
   * members of the group.
   *
   * @param groupId the identifier of the group
   * @param requester the user requesting the balances
   * @return the balances between the members of the group
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   */
  List<BalanceData> getGroupBalances(GroupId groupId, AuthorizedUser requester);

  /**
   * Retrieves the expenses and payments of a group, ordered by completion time descending, type
   * ascending, and ID descending. Requesters with the user role must be members of the group.
   *
   * @param groupId the identifier of the group
   * @param requester the user requesting the transactions
   * @param page the zero-based page index
   * @param size the maximum number of transactions per page
   * @return a page of expense and payment transactions
   * @throws GroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the requester has the user role and is not a member
   *     of the group
   */
  Page<TransactionData> getGroupTransactions(
      GroupId groupId, AuthorizedUser requester, int page, int size);

  /**
   * Recalculates the balances of all the expenses in the group. This is a heavy operation and
   * should be used only when necessary.
   *
   * <p>WARNING! Only use by administrators.
   *
   * @param groupId the identifier of the group
   * @throws GroupNotFoundException if the group does not exist
   */
  void forceSyncBalancesAsAdmin(GroupId groupId);
}
