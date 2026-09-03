package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.dtos.CreateTransactionDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateTransactionDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.properties.GroupInvitationProperties;
import com.familymoney.utils.UUIDGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTransactionGroupService implements TransactionGroupService {

  private final GroupRepository groupRepository;
  private final BalanceRepository balanceRepository;
  private final TransactionRepository transactionRepository;
  private final GroupInvitationRepository groupInvitationRepository;
  private final GroupOperations groupOperations;
  private final Clock clock;
  private final GroupInvitationProperties groupInvitationProperties;

  @Override
  @Transactional
  public GroupId createGroup(
      final GroupName name,
      final Description description,
      final CurrencyUnit currency,
      final UserId createdBy) {
    final GroupId groupId = groupOperations.createGroup(name, description, currency);
    groupRepository
        .addUser(createdBy, groupId)
        .orElseThrow(
            () -> new DatabaseExecutionException("Unable to assign owner to the new group"));
    return groupId;
  }

  @Override
  public void deleteGroup(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    groupOperations.deleteGroup(groupId);
  }

  @Override
  public Page<GroupData> getGroupsByUser(final UserId userId, final Pageable pageable) {
    return groupOperations.getGroupsByUser(userId, pageable);
  }

  @Override
  public GroupData getGroupInfo(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    return groupOperations.getGroupInfo(groupId);
  }

  @Override
  public void updateGroupInfo(
      final GroupId groupId, final UserId userId, final UpdateGroupData data) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    groupOperations.updateGroupInfo(groupId, data);
  }

  @Override
  public GroupInvitationToken getInvitationToken(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    if (groupInvitationRepository.countByGroupIdAndUserId(groupId, userId)
        >= groupInvitationProperties.maxNumInvitations()) {
      throw new MaximumGroupInvitationsReachedException();
    }
    final GroupInvitationToken token = GroupInvitationToken.generate();
    final ExpirationTime expiresAt =
        ExpirationTime.of(Instant.now(clock).plus(groupInvitationProperties.invitationDuration()));
    final UUID invitationId = UUIDGenerator.generate();
    groupInvitationRepository
        .create(new CreateGroupInvitationDto(invitationId, groupId, userId, token, expiresAt))
        .orElseThrow(() -> new DatabaseExecutionException("Unable to create invitation token"));
    return token;
  }

  @Override
  @Transactional
  public void enterToGroupWithToken(final GroupInvitationToken token, final UserId userId) {
    final GroupInvitationEntity invitationDb =
        groupInvitationRepository
            .findByToken(token)
            .orElseThrow(() -> new GroupInvitationInvalidException("Invitation token not found"));
    if (invitationDb.expiresAt().isExpired(clock)) {
      log.info("Invitation token is expired");
      throw new GroupInvitationInvalidException("Invitation token expired");
    }
    groupInvitationRepository.deleteByToken(token);
    groupRepository.addUser(userId, invitationDb.groupId());
  }

  @Override
  public List<UserId> getUsersInGroup(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    return groupOperations.getUsersInGroup(groupId);
  }

  @Override
  public void removeUserFromGroup(
      final GroupId groupId, final UserId userId, final UserId userIdToRemove) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    groupOperations.removeUserFromGroup(groupId, userIdToRemove);
  }

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
  public Page<TransactionData> getGroupTransactions(
      final GroupId groupId, final UserId userId, final Pageable pageable) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    final Page<TransactionEntity> transactionsDb =
        transactionRepository.findAllByGroupId(groupId, pageable);
    return transactionsDb.map(TransactionDataMapper::fromDbo);
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
  public void deleteTransaction(final UserId userId, final TransactionId transactionId) {
    var transactionDb =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    groupOperations.checkIfUserIsInGroup(userId, transactionDb.groupId());
    transactionRepository.deleteById(transactionId);
  }
}
