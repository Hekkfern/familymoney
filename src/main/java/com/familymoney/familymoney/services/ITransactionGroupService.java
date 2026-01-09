package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.services.data.TransactionData;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import jakarta.validation.constraints.Past;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.money.CurrencyUnit;
import org.jspecify.annotations.Nullable;

public interface ITransactionGroupService {

  GroupId createGroup(GroupName name, String description, CurrencyUnit currency, UserId createdBy);

  void deleteGroupOwnedBy(GroupId groupId, UserId groupMember);

  GetGroupData getGroupInfoOwnedBy(GroupId groupId, UserId groupMember);

  boolean isUserInGroup(UserId userId, GroupId groupId);

  void updateGroupInfoOwnedBy(GroupId groupId, UserId groupMember, UpdateGroupData updateGroupData);

  GroupInvitationToken getInvitationTokenOwnedBy(GroupId groupId, UserId groupMember);

  void enterToGroupWithToken(GroupInvitationToken groupInvitationToken, UserId groupMember);

  List<UserId> getUsersInGroupOwnedBy(GroupId groupId, UserId groupMember);

  void removeUserFromGroup(GroupId groupId, UserId groupMember, UserId userIdToRemove);

  Map<UserId, Currency> getGroupBalances(GroupId groupId, UserId groupMember);

  List<TransactionData> getGroupTransactions(GroupId groupId, UserId groupMember);

  void createTransactionInGroup(
      GroupId groupId,
      UserId groupMember,
      String description,
      UUID from,
      UUID to,
      Currency amount,
      Instant doneAt);

  void updateTransactionInGroup(GroupId groupId, UserId groupMember, UpdateTransactionData data);
}
