package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.services.data.TransactionData;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.types.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.money.CurrencyUnit;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
   * @param userId Identifier of the user attempting to delete the group
   */
  void deleteGroup(GroupId groupId, UserId userId);

  GetGroupData getGroupInfo(GroupId groupId, UserId user);

  void updateGroupInfo(GroupId groupId, UserId userId, UpdateGroupData data);

  GroupInvitationToken getInvitationToken(GroupId groupId, UserId userId);

  void enterToGroupWithToken(GroupInvitationToken groupInvitationToken, UserId userId);

  List<UserId> getUsersInGroup(GroupId groupId, UserId userId);

  void removeUserFromGroup(GroupId groupId, UserId userId, UserId userIdToRemove);

  Map<UserId, Money> getGroupBalances(GroupId groupId, UserId userId);

  List<TransactionData> getGroupTransactions(GroupId groupId, UserId userId);

  void createTransactionInGroup(
      GroupId groupId,
      UserId userId,
      String description,
      UUID from,
      UUID to,
      Money amount,
      Instant doneAt);

  void updateTransaction(UserId userId, UpdateTransactionData data);

  void deleteTransaction(UserId id, TransactionId transactionId);

  Page<GetGroupData> getGroups(UserId userId, Pageable pageable);
}
