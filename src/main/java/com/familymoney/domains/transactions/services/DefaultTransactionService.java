package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateTransactionDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateTransactionDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
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

@Service
@RequiredArgsConstructor
public class DefaultTransactionService implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final BalanceRepository balanceRepository;
  private final GroupOperations groupOperations;

  @Override
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
  public Page<TransactionData> getGroupTransactions(
      final GroupId groupId, final UserId userId, final Pageable pageable) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    final Page<TransactionEntity> transactionsDb =
        transactionRepository.findAllByGroupId(groupId, pageable);
    return transactionsDb.map(TransactionDataMapper::fromDbo);
  }

  @Override
  public Page<TransactionData> getGroupTransactionsAsAdmin(
      final GroupId groupId, final Pageable pageable) {
    // TODO
    return null;
  }

  @Override
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
    final TransactionId transactionId = TransactionId.generate();
    transactionRepository.create(
        new CreateTransactionDto(transactionId, description, groupId, amount, from, to, doneAt));
  }

  @Override
  public void createTransactionInGroupAsAdmin(
      final GroupId groupId,
      final Description description,
      final UserId from,
      final UserId to,
      final Money amount,
      final Instant doneAt) {
    // TODO
  }

  @Override
  public void updateTransaction(
      final UserId userId, final TransactionId transactionId, final UpdateTransactionData data) {
    var transactionDb =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    groupOperations.checkIfUserIsInGroup(userId, transactionDb.groupId());
    transactionRepository.updateById(transactionId, UpdateTransactionDataMapper.toDbo(data));
  }

  @Override
  public void updateTransactionAsAdmin(
      final TransactionId transactionId, final UpdateTransactionData data) {
    // TODO
  }

  @Override
  public void deleteTransaction(final UserId userId, final TransactionId transactionId) {
    var transactionDb =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    groupOperations.checkIfUserIsInGroup(userId, transactionDb.groupId());
    transactionRepository.deleteById(transactionId);
  }

  @Override
  public void deleteTransactionAsAdmin(final TransactionId transactionId) {
    // TODO
  }
}
