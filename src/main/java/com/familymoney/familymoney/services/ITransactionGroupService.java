package com.familymoney.familymoney.services;

import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import javax.money.CurrencyUnit;

public interface ITransactionGroupService {

  GroupId createGroup(GroupName name, String description, CurrencyUnit currency, UserId createdBy);

  void deleteGroupOwnedBy(GroupId groupId, UserId userId);

  GetGroupData getGroupInfoOwnedBy(GroupId groupId, UserId userId);

  boolean isUserInGroup(UserId userId, GroupId groupId);
}
