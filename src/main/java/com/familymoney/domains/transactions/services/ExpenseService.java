package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import org.springframework.data.domain.Page;

public interface ExpenseService {

  Page<ExpenseData> getExpenses(GroupId groupId, int page, int size);

  ExpenseId createExpense(
      GroupId groupId,
      Description description,
      Currency currency,
      Instant doneAt,
      Map<UserId, BigDecimal> shares,
      Map<UserId, BigDecimal> payers,
      UserId createdBy);

  ExpenseData getExpense(ExpenseId id);

  void updateExpense(ExpenseId id, UpdateExpenseData data);

  void deleteExpense(ExpenseId id);
}
