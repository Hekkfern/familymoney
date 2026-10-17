package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.repositories.ExpenseRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultExpenseService implements ExpenseService {

  private final ExpenseRepository expenseRepository;

  @Override
  public Page<ExpenseData> getExpenses(final GroupId groupId, final int page, final int size) {
    return null;
  }

  @Override
  public ExpenseId createExpense(
      final GroupId groupId,
      final Description description,
      final Currency currency,
      final Instant doneAt,
      final Map<UserId, BigDecimal> shares,
      final Map<UserId, BigDecimal> payers,
      final UserId createdBy) {
    return null;
  }

  @Override
  public ExpenseData getExpense(final ExpenseId id) {
    return null;
  }

  @Override
  public void updateExpense(final ExpenseId id, final UpdateExpenseData data) {}

  @Override
  public void deleteExpense(final ExpenseId id) {}
}
