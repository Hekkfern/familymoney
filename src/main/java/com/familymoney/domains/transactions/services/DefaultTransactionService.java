package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateTransactionDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultTransactionService implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final BalanceRepository balanceRepository;
  private final GroupOperations groupOperations;

  @Override
  @Transactional
  public Map<UserId, Money> getAllGroupBalances(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    final List<BalanceEntity> balancesDb = balanceRepository.findByGroup(groupId);
    return balancesDb.stream()
        .collect(
            Collectors.toMap(
                b -> b.user1().equals(userId) ? b.user2() : b.user1(),
                BalanceEntity::money,
                (existing, replacement) -> existing));
  }

  @Override
  public Map<UserId, Money> getAllGroupBalancesAsAdmin(final GroupId groupId) {
    // TODO
    return Map.of();
  }

  @Override
  @Transactional
  public Page<TransactionData> getGroupTransactions(
      final GroupId groupId, final UserId userId, final Pageable pageable) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    final Page<ExpenseEntity> transactionsDb =
        transactionRepository.findAllByGroupId(groupId, pageable);
    return transactionsDb.map(TransactionDataMapper::fromDbo);
  }

  @Override
  @Transactional
  public Page<TransactionData> getGroupTransactionsAsAdmin(
      final GroupId groupId, final Pageable pageable) {
    groupOperations.checkIfGroupExists(groupId);
    final Page<ExpenseEntity> transactionsDb =
        transactionRepository.findAllByGroupId(groupId, pageable);
    return transactionsDb.map(TransactionDataMapper::fromDbo);
  }

  @Override
  @Transactional
  public void createTransactionInGroup(
      final GroupId groupId,
      final Description description,
      final UserId from,
      final UserId to,
      final Money amount,
      final Instant doneAt,
      final UserId createdBy) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(createdBy, groupId);
    final ExpenseId expenseId = ExpenseId.generate();
    transactionRepository.create(
        new CreateExpenseDto(expenseId, description, groupId, amount, from, to, doneAt));
  }

  @Override
  @Transactional
  public void createTransactionInGroupAsAdmin(
      final GroupId groupId,
      final Description description,
      final UserId from,
      final UserId to,
      final Money amount,
      final Instant doneAt) {
    groupOperations.checkIfGroupExists(groupId);
    final ExpenseId expenseId = ExpenseId.generate();
    transactionRepository.create(
        new CreateExpenseDto(expenseId, description, groupId, amount, from, to, doneAt));
  }

  @Override
  @Transactional
  public void updateTransaction(
      final UserId userId, final ExpenseId expenseId, final UpdateTransactionData data) {
    var transactionDb =
        transactionRepository
            .findById(expenseId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    groupOperations.checkIfUserIsInGroup(userId, transactionDb.groupId());
    transactionRepository.updateById(expenseId, UpdateTransactionDataMapper.toDbo(data));
  }

  @Override
  public void updateTransactionAsAdmin(
      final ExpenseId expenseId, final UpdateTransactionData data) {
    transactionRepository.updateById(expenseId, UpdateTransactionDataMapper.toDbo(data));
  }

  @Override
  @Transactional
  public void deleteTransaction(final UserId userId, final ExpenseId expenseId) {
    final ExpenseEntity transactionDb =
        transactionRepository
            .findById(expenseId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    groupOperations.checkIfUserIsInGroup(userId, transactionDb.groupId());
    transactionRepository.deleteById(expenseId);
  }

  @Override
  public void deleteTransactionAsAdmin(final ExpenseId expenseId) {
    transactionRepository.deleteById(expenseId);
  }
}
