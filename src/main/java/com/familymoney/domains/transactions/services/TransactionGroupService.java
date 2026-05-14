package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.IBalanceRepository;
import com.familymoney.domains.transactions.repositories.IGroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.IGroupRepository;
import com.familymoney.domains.transactions.repositories.ITransactionRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.dtos.CreateTransactionDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.services.mappers.GroupDataMapper;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateGroupDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateTransactionDataMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.transactions.types.TransactionId;
import com.familymoney.domains.user.exceptions.UserNotFoundException;
import com.familymoney.domains.user.repositories.IUserRepository;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.exceptions.*;
import com.familymoney.testutils.UUIDGenerator;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionGroupService implements ITransactionGroupService {

  private static final Duration INVITATION_TOKEN_EXPIRY = Duration.ofHours(24);

  private final IGroupRepository groupRepository;
  private final IBalanceRepository balanceRepository;
  private final ITransactionRepository transactionRepository;
  private final IGroupInvitationRepository groupInvitationRepository;
  private final IUserRepository userRepository;
  private final Clock clock;

  @Override
  @Transactional
  public GroupId createGroup(
      GroupName name, String description, CurrencyUnit currency, UserId createdBy) {
    // Create group in the database
    val groupId = GroupId.generate();
    groupRepository
        .create(
            CreateGroupDto.builder()
                .id(groupId)
                .name(name)
                .description(description)
                .currency(currency)
                .build())
        .orElseThrow(() -> new DatabaseExecutionException("Unable to create group"));
    // Add creator to the group
    groupRepository
        .addUser(createdBy, groupId)
        .orElseThrow(
            () -> new DatabaseExecutionException("Unable to assign owner to the new group"));
    return groupId;
  }

  /**
   * Checks if a user is a member of a group, throwing an exception if not.
   *
   * @param userId Identifier of the user
   * @param groupId Identifier of the group
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  private void checkIfUserIsInGroup(UserId userId, GroupId groupId)
      throws UserIsNotMemberOfGroupException {
    if (!groupRepository.isUserInGroup(userId, groupId)) {
      log.info("User {} is not a member of group {}", userId, groupId);
      throw new UserIsNotMemberOfGroupException(
          String.format("User %s is not a member of group %s", userId, groupId));
    }
  }

  /**
   * Checks if a group exists, throwing an exception if not.
   *
   * @param groupId Identifier of the group
   * @throws TransactionGroupNotFoundException if the group does not exist
   */
  private void checkIfGroupExists(GroupId groupId) {
    val exists = groupRepository.existsById(groupId);
    if (!exists) {
      log.info("Group {} does not exist", groupId);
      throw new TransactionGroupNotFoundException(
          String.format("Group %s does not exist", groupId));
    }
  }

  private void checkIfUserExists(UserId userId) {
    val exists = userRepository.existsById(userId);
    if (!exists) {
      log.info("User {} does not exist", userId);
      throw new UserNotFoundException(String.format("Group %s does not exist", userId));
    }
  }

  @Override
  public void deleteGroup(GroupId groupId, UserId userId) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Delete group
    groupRepository.deleteById(groupId);
  }

  @Override
  public Page<GroupData> getGroupsByUser(final UserId userId, final Pageable pageable) {
    return groupRepository.findByUserId(userId, pageable).map(GroupDataMapper::fromDbo);
  }

  @Override
  public GroupData getGroupInfo(GroupId groupId, UserId userId) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get data
    return groupRepository
        .findById(groupId)
        .map(GroupDataMapper::fromDbo)
        .orElseThrow(
            () ->
                new TransactionGroupNotFoundException(
                    String.format("Unable to find group %s", groupId)));
  }

  @Override
  public void updateGroupInfo(GroupId groupId, UserId userId, UpdateGroupData data) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Update data
    groupRepository.updateById(groupId, UpdateGroupDataMapper.toDbo(data));
  }

  @Override
  public GroupInvitationToken getInvitationToken(GroupId groupId, UserId userId) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Generate token
    val token = GroupInvitationToken.generate();
    val expiresAt = Instant.now(clock).plus(INVITATION_TOKEN_EXPIRY);
    val invitationId = UUIDGenerator.generate();
    groupInvitationRepository
        .create(
            CreateGroupInvitationDto.builder()
                .id(invitationId)
                .groupId(groupId)
                .token(token)
                .expiresAt(expiresAt)
                .build())
        .orElseThrow(() -> new DatabaseExecutionException("Unable to create invitation token"));
    return token;
  }

  @Override
  @Transactional
  public void enterToGroupWithToken(GroupInvitationToken token, UserId userId) {
    // Get the invitation, if it exists
    val invitationDb =
        groupInvitationRepository
            .findByToken(token)
            .orElseThrow(() -> new GroupInvitationInvalidException("Invitation token not found"));
    // Check if the invitation is expired
    if (invitationDb.expiresAt().isBefore(Instant.now(clock))) {
      log.info("Invitation token {} is expired", token);
      throw new GroupInvitationInvalidException("Invitation token expired");
    }
    // Remove token after use
    groupInvitationRepository.deleteByToken(token);
    // Add user to group
    groupRepository.addUser(userId, invitationDb.groupId());
  }

  @Override
  public List<UserId> getUsersInGroup(GroupId groupId, UserId userId) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get users in group
    return groupRepository.findUserIdsByGroupId(groupId);
  }

  @Override
  public void removeUserFromGroup(GroupId groupId, UserId userId, UserId userIdToRemove) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user to remove exists
    checkIfUserExists(userIdToRemove);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Remove user from group
    groupRepository.deleteUser(userIdToRemove, groupId);
  }

  @Override
  public Map<UserId, Money> getAllGroupBalances(GroupId groupId, UserId userId) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get balances
    val balancesDb = balanceRepository.findByGroup(groupId);
    // Map balances to user money map
    return balancesDb.stream()
        .collect(
            Collectors.toMap(
                b -> b.user1().equals(userId) ? b.user2() : b.user1(),
                BalanceEntity::amount,
                (existing, replacement) -> existing));
  }

  @Override
  public Page<TransactionData> getGroupTransactions(
      GroupId groupId, UserId userId, Pageable pageable) {
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(userId, groupId);
    // Get transactions
    val transactionsDb = transactionRepository.findAllByGroupId(groupId, pageable);
    // Generate result
    return transactionsDb.map(TransactionDataMapper::fromDbo);
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
    // Check if the group exists
    checkIfGroupExists(groupId);
    // Check if the user is a member of the group
    checkIfUserIsInGroup(createdBy, groupId);
    // create transaction
    val transactionId = TransactionId.generate();
    transactionRepository.create(
        CreateTransactionDto.builder()
            .id(transactionId)
            .description(description)
            .groupId(groupId)
            .amount(amount)
            .lender(from)
            .borrower(to)
            .doneAt(doneAt)
            .build());
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
    transactionRepository.updateById(transactionId, UpdateTransactionDataMapper.toDbo(data));
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
