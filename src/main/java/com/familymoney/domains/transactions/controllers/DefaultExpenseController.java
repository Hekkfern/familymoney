package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.utils.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultExpenseController implements ExpenseController {

  @Override
  public PageResponse<ExpenseDto> getExpenses(final UUID groupId, final int page, final int size) {
    return null;
  }

  @Override
  public void createExpense(final String idempotencyKey,
      final UUID groupId, final CreateExpenseRequestDto request, HttpServletRequest httpRequest) {}

  @Override
  public ExpenseDto getExpense(final UUID expenseId) {
    return null;
  }

  @Override
  public void updateExpense(final UUID expenseId, final UpdateExpenseRequestDto request) {}

  @Override
  public void deleteExpense(final UUID expenseId) {}
}
