package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.ExpenseNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.repositories.ExpenseRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dbos.CreateExpenseDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdateExpenseDbo;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.services.mappers.ExpenseDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final GroupChecker groupChecker;

  @Override
  @Transactional(readOnly = true)
  public Page<ExpenseData> getExpenses(
      final GroupId groupId, final AuthorizedUser requester, final int page, final int size) {
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    final Currency currency = getGroupCurrency(groupId);
    return expenseRepository
        .findAllByGroupId(groupId, PageRequest.of(page, size))
        .map(entity -> ExpenseDataMapper.fromDbo(entity, currency));
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
    groupChecker.checkIfGroupExists(groupId);
    final ExpenseId id = ExpenseId.generate();
    expenseRepository.create(
        new CreateExpenseDbo(id, description, groupId, requester.id(), doneAt, shares, payers));
    return id;
  }

  @Override
  @Transactional(readOnly = true)
  public ExpenseData getExpense(final ExpenseId id, final AuthorizedUser requester) {
    final FullExpenseEntity entity = getExpenseFromId(id);
    final GroupId groupId = entity.expense().groupId();
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    return ExpenseDataMapper.fromDbo(entity, getGroupCurrency(entity.expense().groupId()));
  }

  @Override
  public void updateExpense(
      final ExpenseId id, final AuthorizedUser requester, final UpdateExpenseData data) {
    final FullExpenseEntity entity = getExpenseFromId(id);
    final GroupId groupId = entity.expense().groupId();
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    expenseRepository.updateById(
        id, new UpdateExpenseDbo(data.description(), data.doneAt(), data.shares(), data.payers()));
  }

  @Override
  public void deleteExpense(final ExpenseId id, final AuthorizedUser requester) {
    final FullExpenseEntity entity = getExpenseFromId(id);
    final GroupId groupId = entity.expense().groupId();
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
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

  private FullExpenseEntity getExpenseFromId(final ExpenseId id) {
    return expenseRepository
        .findById(id)
        .orElseThrow(
            () -> {
              final String message = "Expense with ID '%s' does not exist".formatted(id.value());
              log.info(message);
              return new ExpenseNotFoundException(message);
            });
  }
}
