package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupMembersReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupsPerUserReachedException;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.mappers.GroupDataMapper;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateGroupDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.properties.GroupProperties;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.UUIDGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
public class DefaultGroupService implements GroupService {

  private final GroupRepository groupRepository;
  private final GroupInvitationRepository groupInvitationRepository;
  private final GroupChecker groupChecker;
  private final Clock clock;
  private final GroupProperties groupProperties;

  @Override
  public GroupId createEmptyGroup(
      final GroupName name,
      final Description description,
      final Currency currency,
      final AuthorizedUser requester) {
    final GroupId groupId = GroupId.generate();
    groupRepository.create(
        new CreateGroupDto(groupId, name, description, currency, requester.id()));
    return groupId;
  }

  @Override
  @Transactional
  public GroupId createGroupAndAddCreatorAsMember(
      final GroupName name,
      final Description description,
      final Currency currency,
      final AuthorizedUser requester) {
    try {
      final GroupId groupId = GroupId.generate();
      groupRepository.create(
          new CreateGroupDto(groupId, name, description, currency, requester.id()));
      groupRepository.addUserToGroup(requester.id(), groupId);
      return groupId;
    } catch (final UserNotFoundException e) {
      throw new GroupOwnerNotFoundException("User does not exist", e);
    }
  }

  @Override
  @Transactional
  public void leaveGroup(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.checkIfGroupExists(groupId);
    groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    groupRepository.removeUserFromGroup(requester.id(), groupId);
  }

  @Override
  @Transactional
  public void deleteGroupAsAdmin(final GroupId groupId) {
    groupChecker.checkIfGroupExists(groupId);
    groupRepository.softDeleteById(groupId);
  }

  @Override
  @Transactional
  public Page<GroupData> getGroupsForUser(final UserId userId, final Pageable pageable) {
    groupChecker.checkIfUserExists(userId);
    return groupRepository.findByUserId(userId, pageable).map(GroupDataMapper::fromDbo);
  }

  @Override
  @Transactional
  public GroupData getGroupInfo(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    return groupRepository
        .findById(groupId)
        .map(GroupDataMapper::fromDbo)
        .orElseThrow(
            () -> new GroupNotFoundException(String.format("Unable to find group %s", groupId)));
  }

  @Override
  @Transactional
  public void updateGroupInfo(
      final GroupId groupId, final AuthorizedUser requester, final UpdateGroupData data) {
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    groupRepository.updateById(groupId, UpdateGroupDataMapper.toDbo(data));
  }

  @Override
  @Transactional
  public GroupInvitationToken getInvitationToken(
      final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    if (groupRepository.countMembershipsOfGroup(groupId) >= groupProperties.maxNumUsersPerGroup()) {
      throw new MaximumGroupMembersReachedException();
    }
    if (groupInvitationRepository.countByGroupIdAndUserId(groupId, requester.id())
        >= groupProperties.maxNumInvitations()) {
      throw new MaximumGroupInvitationsReachedException();
    }
    final GroupInvitationToken token = GroupInvitationToken.generate();
    final ExpirationTime expiresAt =
        ExpirationTime.of(Instant.now(clock).plus(groupProperties.invitationDuration()));
    final UUID invitationId = UUIDGenerator.generate();
    groupInvitationRepository.create(
        new CreateGroupInvitationDto(
            invitationId, groupId, requester.id(), token, requester.id(), expiresAt));
    return token;
  }

  @Override
  @Transactional
  public void enterToGroupWithToken(
      final GroupInvitationToken token, final AuthorizedUser requester) {
    final GroupInvitationEntity invitationDb =
        groupInvitationRepository
            .findByToken(token)
            .orElseThrow(() -> new GroupInvitationInvalidException("Invitation token not found"));
    if (invitationDb.expiresAt().isExpired(clock)) {
      log.info("Invitation token is expired");
      throw new GroupInvitationInvalidException("Invitation token expired");
    }
    if (groupRepository.countMembershipsOfGroup(invitationDb.groupId())
        >= groupProperties.maxNumUsersPerGroup()) {
      throw new MaximumGroupMembersReachedException();
    }
    if (groupRepository.countMembershipsOfUser(requester.id())
        >= groupProperties.maxNumGroupsPerUser()) {
      throw new MaximumGroupsPerUserReachedException();
    }
    groupChecker.checkIfGroupExists(invitationDb.groupId());
    groupInvitationRepository.deleteByToken(token);
    groupRepository.addUserToGroup(requester.id(), invitationDb.groupId());
  }

  @Override
  @Transactional
  public List<MemberData> getUsersInGroup(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.checkIfGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.checkIfUserIsInGroup(requester.id(), groupId);
    }
    return groupRepository.findUsersByGroupId(groupId).stream().map(UserData);
  }

  @Override
  @Transactional
  public void addUserToGroupAsAdmin(final GroupId groupId, final UserId userIdToAdd) {
    groupChecker.checkIfGroupExists(groupId);
    groupChecker.checkIfUserExists(userIdToAdd);
    if (groupRepository.countMembershipsOfGroup(groupId) >= groupProperties.maxNumUsersPerGroup()) {
      throw new MaximumGroupMembersReachedException();
    }
    if (groupRepository.countMembershipsOfUser(userIdToAdd)
        >= groupProperties.maxNumGroupsPerUser()) {
      throw new MaximumGroupsPerUserReachedException();
    }
    groupRepository.addUserToGroup(userIdToAdd, groupId);
  }

  @Override
  @Transactional
  public Map<UserId, Money> getAllGroupBalances(final GroupId groupId, final UserId userId) {
    groupChecker.checkIfGroupExists(groupId);
    groupChecker.checkIfUserIsInGroup(userId, groupId);
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
    groupChecker.checkIfGroupExists(groupId);
    groupChecker.checkIfUserIsInGroup(userId, groupId);
    final Page<ExpenseEntity> transactionsDb =
        transactionRepository.findAllByGroupId(groupId, pageable);
    return transactionsDb.map(TransactionDataMapper::fromDbo);
  }

  @Override
  @Transactional
  public Page<TransactionData> getGroupTransactionsAsAdmin(
      final GroupId groupId, final Pageable pageable) {
    groupChecker.checkIfGroupExists(groupId);
    final Page<ExpenseEntity> transactionsDb =
        transactionRepository.findAllByGroupId(groupId, pageable);
    return transactionsDb.map(TransactionDataMapper::fromDbo);
  }
}
