package com.familymoney.domains.transactions.repositories.dbos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record CreateExpenseDbo(
    ExpenseId id,
    Description description,
    GroupId groupId,
    UserId createdBy,
    Instant doneAt,
    Map<UserId, BigDecimal> shares,
    Map<UserId, BigDecimal> payers) {

  public CreateExpenseDbo {
    ExpenseAmounts.validate(shares, payers);
    shares = Map.copyOf(shares);
    payers = Map.copyOf(payers);
  }
}
