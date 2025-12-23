package com.familymoney.familymoney.services;

import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import javax.money.CurrencyUnit;

public interface ITransactionGroupService {

  void createGroup(GroupName name, String description, CurrencyUnit currency, UserId createdBy);
}
