package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.domain.Page;

public interface ExpenseService {

  Page<ExpenseData> getExpenses(GroupId groupId, AuthorizedUser requester, int page, int size);

  ExpenseId createExpense(
      GroupId groupId,
      Description description,
      Instant doneAt,
      Map<UserId, BigDecimal> shares,
      Map<UserId, BigDecimal> payers,
      AuthorizedUser requester);

  ExpenseData getExpense(ExpenseId id, AuthorizedUser requester);

  void updateExpense(ExpenseId id, AuthorizedUser requester, UpdateExpenseData data);

  void deleteExpense(ExpenseId id, AuthorizedUser requester);
}
