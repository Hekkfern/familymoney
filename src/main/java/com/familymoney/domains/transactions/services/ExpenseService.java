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
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

  Page<ExpenseData> getExpenses(final GroupId groupId, final Pageable pageable);

  ExpenseId createExpense(
      final GroupId groupId,
      final Description description,
      final Currency currency,
      final Instant doneAt,
      final Map<UserId, BigDecimal> shares,
      final Map<UserId, BigDecimal> payers,
      final UserId createdBy);

  ExpenseData getExpense(final ExpenseId id);

  void updateExpense(final ExpenseId id, final UpdateExpenseData data);

  void deleteExpense(final ExpenseId id);
}
