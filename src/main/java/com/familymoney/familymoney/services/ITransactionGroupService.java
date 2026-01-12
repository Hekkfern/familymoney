package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.services.data.TransactionData;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.utils.AuthorizedUser;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.money.CurrencyUnit;
import org.javamoney.moneta.Money;

public interface ITransactionGroupService {

  /**
   * Create a new transaction group.
   *
   * @param name Name of the group to display to users
   * @param description Description of the group
   * @param currency Default currency for the group
   * @param createdBy Identifier of the user creating the group
   * @return Identifier of the newly created group
   */
  GroupId createGroup(GroupName name, String description, CurrencyUnit currency, UserId createdBy);

  /**
   * Tries to delete a transaction group. It can fail if the user is not authorized to delete the
   * group, or if the group does not exist
   *
   * @param groupId Identifier of the group to delete
   * @param user User attempting to delete the group
   */
  void deleteGroup(GroupId groupId, AuthorizedUser user);

  GetGroupData getGroupInfo(GroupId groupId, AuthorizedUser user);

  boolean isUserInGroup(UserId userId, GroupId groupId);

  void updateGroupInfo(GroupId groupId, AuthorizedUser user, UpdateGroupData updateGroupData);

  GroupInvitationToken getInvitationToken(GroupId groupId, AuthorizedUser user);

  void enterToGroupWithToken(GroupInvitationToken groupInvitationToken, AuthorizedUser user);

  List<UserId> getUsersInGroup(GroupId groupId, AuthorizedUser user);

  void removeUserFromGroup(GroupId groupId, AuthorizedUser user, UserId userIdToRemove);

  Map<UserId, Money> getGroupBalances(GroupId groupId, AuthorizedUser user);

  List<TransactionData> getGroupTransactions(GroupId groupId, AuthorizedUser user);

  void createTransactionInGroup(
      GroupId groupId,
      AuthorizedUser user,
      String description,
      UUID from,
      UUID to,
      Money amount,
      Instant doneAt);

  void updateTransactionInGroup(GroupId groupId, AuthorizedUser user, UpdateTransactionData data);
}
