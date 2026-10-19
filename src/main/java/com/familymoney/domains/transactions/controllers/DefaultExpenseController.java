package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.ExpenseDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdateExpenseRequestMapper;
import com.familymoney.domains.transactions.services.ExpenseService;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.familymoney.utils.PageResponseMapper;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultExpenseController implements ExpenseController {

  private final ExpenseService expenseService;

  @Override
  public PageResponse<ExpenseDto> getExpenses(final UUID groupId, final int page, final int size) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Page<ExpenseData> expensePages =
        expenseService.getExpenses(GroupId.fromUuid(groupId), user, page, size);
    return PageResponseMapper.from(expensePages.map(ExpenseDtoMapper::toDto));
  }

  @Override
  public CreateExpenseResponseDto createExpense(
      final UUID groupId, final CreateExpenseRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final ExpenseId expenseId =
        expenseService.createExpense(
            GroupId.fromUuid(groupId),
            Description.of(request.description()),
            request.doneAt(),
            request.shares().stream()
                .collect(
                    Collectors.toMap(
                        share -> UserId.fromUuid(share.userId()),
                        CreateExpenseRequestDto.ExpenseShareDto::amount)),
            request.payers().stream()
                .collect(
                    Collectors.toMap(
                        payer -> UserId.fromUuid(payer.userId()),
                        CreateExpenseRequestDto.ExpensePayerDto::amount)),
            user);
    return new CreateExpenseResponseDto(expenseId.value());
  }

  @Override
  public ExpenseDto getExpense(final UUID expenseId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final ExpenseData expenseData = expenseService.getExpense(ExpenseId.fromUuid(expenseId), user);
    return ExpenseDtoMapper.toDto(expenseData);
  }

  @Override
  public void updateExpense(final UUID expenseId, final UpdateExpenseRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    expenseService.updateExpense(
        ExpenseId.fromUuid(expenseId), user, UpdateExpenseRequestMapper.fromDto(request));
  }

  @Override
  public void deleteExpense(final UUID expenseId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    expenseService.deleteExpense(ExpenseId.fromUuid(expenseId), user);
  }
}
