package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.ExpenseNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.repositories.ExpenseRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpensePaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseShareEntity;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultExpenseService implements ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final GroupRepository groupRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<ExpenseData> getExpenses(
      final GroupId groupId, final AuthorizedUser requester, final int page, final int size) {
    final Currency currency = getGroupCurrency(groupId);
    return expenseRepository
        .findAllByGroupId(groupId, PageRequest.of(page, size))
        .map(entity -> toExpenseData(entity, currency));
  }

  @Override
  @Transactional
  public ExpenseId createExpense(
      final GroupId groupId,
      final Description description,
      final Instant doneAt,
      final Map<UserId, BigDecimal> shares,
      final Map<UserId, BigDecimal> payers,
      final AuthorizedUser requester) {
    final ExpenseId id = ExpenseId.generate();
    expenseRepository.create(
        new CreateExpenseDto(id, description, groupId, requester.id(), doneAt, shares, payers));
    return id;
  }

  @Override
  @Transactional(readOnly = true)
  public ExpenseData getExpense(final ExpenseId id, final AuthorizedUser requester) {
    final FullExpenseEntity entity =
        expenseRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  final String message =
                      "Expense with ID '%s' does not exist".formatted(id.value());
                  log.info(message);
                  return new ExpenseNotFoundException(message);
                });
    return toExpenseData(entity, getGroupCurrency(entity.expense().groupId()));
  }

  @Override
  public void updateExpense(
      final ExpenseId id, final AuthorizedUser requester, final UpdateExpenseData data) {
    expenseRepository.updateById(
        id, new UpdateExpenseDto(data.description(), data.doneAt(), data.shares(), data.payers()));
  }

  @Override
  public void deleteExpense(final ExpenseId id, final AuthorizedUser requester) {
    expenseRepository.deleteById(id);
  }

  private Currency getGroupCurrency(final GroupId groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(
            () -> {
              final String message = "Group with ID '%s' does not exist".formatted(groupId.value());
              log.info(message);
              return new GroupNotFoundException(message);
            })
        .currency();
  }

  private static ExpenseData toExpenseData(
      final FullExpenseEntity entity, final Currency currency) {
    final ExpenseEntity expense = entity.expense();
    final Map<UserId, BigDecimal> shares =
        entity.shares().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    ExpenseShareEntity::userId, ExpenseShareEntity::amount));
    final Map<UserId, BigDecimal> payers =
        entity.payments().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    ExpensePaymentEntity::userId, ExpensePaymentEntity::amount));
    final BigDecimal totalAmount =
        shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return new ExpenseData(
        expense.id(),
        expense.groupId(),
        expense.description(),
        Money.of(totalAmount, currency.getCurrencyCode()),
        expense.doneAt(),
        shares,
        payers);
  }
}
