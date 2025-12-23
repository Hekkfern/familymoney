package com.familymoney.familymoney.services;

import com.familymoney.familymoney.repositories.IBalanceRepository;
import com.familymoney.familymoney.repositories.IGroupRepository;
import com.familymoney.familymoney.repositories.ITransactionRepository;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionGroupService implements ITransactionGroupService {

  private final IGroupRepository groupRepository;
  private final IBalanceRepository balanceRepository;
  private final ITransactionRepository transactionRepository;

  @Override
  public GroupId createGroup(
      GroupName name, String description, CurrencyUnit currency, UserId createdBy) {
    // Create group in the database
    val group = groupRepository.create(name, description, currency, createdBy);
  }
}
