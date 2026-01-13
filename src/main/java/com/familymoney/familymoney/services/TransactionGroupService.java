package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.GroupInvitationNotFoundException;
import com.familymoney.familymoney.exceptions.GroupNotOwnedByUserException;
import com.familymoney.familymoney.repositories.IBalanceRepository;
import com.familymoney.familymoney.repositories.IGroupInvitationRepository;
import com.familymoney.familymoney.repositories.IGroupRepository;
import com.familymoney.familymoney.repositories.ITransactionRepository;
import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.services.data.TransactionData;
import com.familymoney.familymoney.services.data.UpdateGroupData;
import com.familymoney.familymoney.services.data.UpdateTransactionData;
import com.familymoney.familymoney.services.mappers.GetGroupDataMapper;
import com.familymoney.familymoney.services.mappers.UpdateGroupDataMapper;
import com.familymoney.familymoney.types.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    val invitationDbo =
        groupInvitationRepository
            .create(groupId, token, expiresAt)
            .orElseThrow(() -> new DatabaseExecutionException("Unable to create invitation token"));
    return invitationDbo.token();
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
    // TODO
    return List.of();
  }

  @Override
  public void removeUserFromGroup(GroupId groupId, UserId userId, UserId userIdToRemove) {
    // TODO

  }

  @Override
  public Map<UserId, Money> getGroupBalances(GroupId groupId, UserId userId) {
    // TODO
    return Map.of();
  }

  @Override
  public List<TransactionData> getGroupTransactions(GroupId groupId, UserId userId) {
    // TODO
    return List.of();
  }

  @Override
  public void createTransactionInGroup(
      GroupId groupId,
      UserId userId,
      String description,
      UUID from,
      UUID to,
      Money amount,
      Instant doneAt) {
    // TODO
  }

  @Override
  public void updateTransaction(UserId userId, UpdateTransactionData data) {
    // TODO
  }

  @Override
  public void deleteTransaction(UserId id, TransactionId transactionId) {
    // TODO
  }

  @Override
  public Page<GetGroupData> getGroups(UserId userId, Pageable pageable) {
    // TODO
    return null;
  }
}
