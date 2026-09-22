package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.utils.PageResponse;
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
  public void createExpense(final UUID groupId, final CreateExpenseRequestDto request) {}

  @Override
  public ExpenseDto getExpense(final UUID expenseId) {
    return null;
  }

  @Override
  public void updateExpense(final UUID expenseId, final UpdateExpenseRequestDto request) {}

  @Override
  public void deleteExpense(final UUID expenseId) {}
}
