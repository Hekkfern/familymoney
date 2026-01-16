package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.GroupInvitationNotFoundException;
import com.familymoney.familymoney.exceptions.GroupNotOwnedByUserException;
import com.familymoney.familymoney.exceptions.TransactionNotFoundException;
import com.familymoney.familymoney.repositories.IBalanceRepository;
import com.familymoney.familymoney.repositories.IGroupInvitationRepository;
import com.familymoney.familymoney.repositories.IGroupRepository;
import com.familymoney.familymoney.repositories.ITransactionRepository;
import com.familymoney.familymoney.repositories.dbos.BalanceDbo;
import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.services.data.TransactionData;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.services.mappers.GetGroupDataMapper;
import com.familymoney.familymoney.services.mappers.GetTransactionDataMapper;
import com.familymoney.familymoney.services.mappers.UpdateGroupDataMapper;
import com.familymoney.familymoney.services.mappers.UpdateTransactionDataMapper;
import com.familymoney.familymoney.types.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionGroupService implements ITransactionGroupService {

  private static final Duration INVITATION_TOKEN_EXPIRY = Duration.ofHours(24);

  private final IGroupRepository groupRepository;
  private final IBalanceRepository balanceRepository;
  private final ITransactionRepository transactionRepository;
  private final IGroupInvitationRepository groupInvitationRepository;
  private final GetGroupDataMapper getGroupDataMapper;
  private final UpdateGroupDataMapper updateGroupDataMapper;
  private final GetTransactionDataMapper getTransactionDataMapper;
  private final UpdateTransactionDataMapper updateTransactionDataMapper;
  private final Clock clock;

  @Override
  public GroupId createGroup(
      GroupName name, String description, CurrencyUnit currency, UserId createdBy) {
    // Create group in the database
    val group =
        groupRepository
            .create(name, description, currency, createdBy)
            .orElseThrow(() -> new DatabaseExecutionException("Unable to create group"));
    return group.id();
  }

  private void checkIfUserIsInGroup(UserId userId, GroupId groupId)
      throws GroupNotOwnedByUserException {
    if (!groupRepository.isUserInGroup(userId, groupId)) {
      log.info("User {} is not a member of group {}", userId, groupId);
      throw new GroupNotOwnedByUserException(
          String.format("User %s is not a member of group %s", userId, groupId));
    }
  }

  @Override
  public void deleteGroup(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Delete group
    groupRepository.deleteById(groupId);
  }

  @Override
  public Page<GetGroupData> getGroups(UserId userId, Pageable pageable) {
    return groupRepository.findByUserId(userId, pageable).map(getGroupDataMapper::fromDbo);
  }

  @Override
  public GetGroupData getGroupInfo(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get data
    return groupRepository
        .findById(groupId)
        .map(getGroupDataMapper::fromDbo)
        .orElseThrow(
            () ->
                new DatabaseExecutionException(String.format("Unable to find group %s", groupId)));
  }

  @Override
  public void updateGroupInfo(GroupId groupId, UserId userId, UpdateGroupData data) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Update data
    groupRepository.updateById(groupId, updateGroupDataMapper.toDbo(data));
  }

  @Override
  public GroupInvitationToken getInvitationToken(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Generate token
    val token = GroupInvitationToken.generate();

    val expiresAt = Instant.now(clock).plus(INVITATION_TOKEN_EXPIRY);
    val invitationDb =
        groupInvitationRepository
            .create(groupId, token, expiresAt)
            .orElseThrow(() -> new DatabaseExecutionException("Unable to create invitation token"));
    return invitationDb.token();
  }

  @Override
  public void enterToGroupWithToken(GroupInvitationToken token, UserId userId) {
    // Get the invitation, if it exists
    val invitationDb =
        groupInvitationRepository
            .findByToken(token)
            .orElseThrow(() -> new GroupInvitationNotFoundException("Invitation token not found"));
    // Check if the invitation is expired
    if (invitationDb.expiresAt().isBefore(Instant.now(clock))) {
      log.info("Invitation token {} is expired", token);
      throw new GroupInvitationNotFoundException("Invitation token expired");
    }
    // Remove token after use
    groupInvitationRepository.deleteByToken(token);
    // Add user to group
    groupRepository.addUser(userId, invitationDb.groupId());
  }

  @Override
  public List<UserId> getUsersInGroup(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get users in group
    return groupRepository.findUserIdsByGroupId(groupId);
  }

  @Override
  public void removeUserFromGroup(GroupId groupId, UserId userId, UserId userIdToRemove) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Remove user from group
    groupRepository.deleteUser(userId, groupId);
  }

  @Override
  public Map<UserId, Money> getAllGroupBalances(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get balances
    val balancesDb = balanceRepository.findByGroup(groupId);
    // Map balances to user money map
    return balancesDb.stream()
        .collect(
            Collectors.toMap(
                b -> b.user1().equals(userId) ? b.user2() : b.user1(),
                BalanceDbo::amount,
                (existing, replacement) -> existing));
  }

  @Override
  public Page<TransactionData> getGroupTransactions(
      GroupId groupId, UserId userId, Pageable pageable) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get transactions
    val transactionsDb = transactionRepository.findAllByGroupId(groupId, pageable);
    // Generate result
    return transactionsDb.map(getTransactionDataMapper::fromDbo);
  }

  @Override
  public void createTransactionInGroup(
      GroupId groupId,
      String description,
      UserId from,
      UserId to,
      Money amount,
      Instant doneAt,
      UserId createdBy) {
    // Check if the user is a member of the group
    checkIfUserIsInGroup(createdBy, groupId);
    // create transaction
    transactionRepository.create(description, groupId, amount, from, to, doneAt);
  }

  @Override
  public void updateTransaction(
      UserId userId, TransactionId transactionId, UpdateTransactionData data) {
    // Get transaction
    var transactionDb =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, transactionDb.groupId());
    // Update transaction
    transactionRepository.updateById(transactionId, updateTransactionDataMapper.toDbo(data));
  }

  @Override
  public void deleteTransaction(UserId userId, TransactionId transactionId) {
    // Get transaction
    var transactionDb =
        transactionRepository
            .findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, transactionDb.groupId());
    // Delete transaction
    transactionRepository.deleteById(transactionId);
  }
}
