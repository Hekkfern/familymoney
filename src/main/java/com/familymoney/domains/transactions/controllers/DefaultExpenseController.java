package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.idempotency.services.IdempotencyService;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultExpenseController implements ExpenseController {

  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  @Override
  public PageResponse<ExpenseDto> getExpenses(final UUID groupId, final int page, final int size) {
    // TODO
    return null;
  }

  @Override
  public CreateExpenseResponseDto createExpense(
      final String idempotencyKey,
      final UUID groupId,
      final CreateExpenseRequestDto request,
      HttpServletRequest httpRequest) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final IdempotencyKey key = IdempotencyKey.fromString(idempotencyKey);
    return idempotencyService.runWithIdempotency(
        key,
        user.id(),
        httpRequest,
        request,
        objectMapper.constructType(CreateExpenseResponseDto.class),
        () -> {
          // TODO
        });
  }

  @Override
  public ExpenseDto getExpense(final UUID expenseId) {
    // TODO
    return null;
  }

  @Override
  public void updateExpense(final UUID expenseId, final UpdateExpenseRequestDto request) {
    // TODO
  }

  @Override
  public void deleteExpense(final UUID expenseId) {
    // TODO
  }
}
