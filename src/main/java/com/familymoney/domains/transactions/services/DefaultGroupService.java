package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.events.GroupInvitationRequestedEvent;
import com.familymoney.domains.transactions.exceptions.ExpenseNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupMembersReachedException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupsPerUserReachedException;
import com.familymoney.domains.transactions.exceptions.PaymentNotFoundException;
import com.familymoney.domains.transactions.exceptions.SendGroupInvitationTooSoonException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInGroupException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInvitedException;
import com.familymoney.domains.transactions.exceptions.WrongUserForInvitationException;
import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.ExpenseRepository;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.PaymentRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import com.familymoney.domains.transactions.repositories.dbos.CreateGroupDbo;
import com.familymoney.domains.transactions.repositories.dbos.CreateGroupInvitationDbo;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionReference;
import com.familymoney.domains.transactions.services.data.BalanceData;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.TransactionType;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.mappers.GroupDataMapper;
import com.familymoney.domains.transactions.services.mappers.MemberDataMapper;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateGroupDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.properties.GroupProperties;
import com.familymoney.utils.AuthorizedUser;
import java.time.Clock;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupService implements GroupService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final BalanceRepository balanceRepository;
  private final TransactionRepository transactionRepository;
  private final ExpenseRepository expenseRepository;
  private final PaymentRepository paymentRepository;
  private final GroupInvitationRepository groupInvitationRepository;
  private final GroupChecker groupChecker;
  private final Clock clock;
  private final GroupProperties groupProperties;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public GroupId createEmptyGroup(
      final GroupName name,
      final Description description,
      final Currency currency,
      final AuthorizedUser requester) {
    final GroupId groupId = GroupId.generate();
    groupRepository.create(
        new CreateGroupDbo(groupId, name, description, currency, requester.id()));
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
          new CreateGroupDbo(groupId, name, description, currency, requester.id()));
      groupRepository.addUserToGroup(requester.id(), groupId);
      return groupId;
    } catch (final UserNotFoundException e) {
      throw new GroupOwnerNotFoundException("User does not exist", e);
    }
  }

  @Override
  @Transactional
  public void leaveGroup(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.verifyGroupExists(groupId);
    groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    groupRepository.removeUserFromGroup(requester.id(), groupId);
  }

  @Override
  @Transactional
  public void deleteGroupAsAdmin(final GroupId groupId) {
    groupChecker.verifyGroupExists(groupId);
    groupRepository.softDeleteById(groupId);
  }

  @Override
  @Transactional
  public Page<GroupData> getGroupsForUser(final UserId userId, final int page, final int size) {
    groupChecker.verifyUserExists(userId);
    return groupRepository
        .findByUserId(userId, PageRequest.of(page, size))
        .map(GroupDataMapper::fromDbo);
  }

  @Override
  @Transactional
  public GroupData getGroupInfo(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.verifyGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
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
    groupChecker.verifyGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    }
    groupRepository.updateById(groupId, UpdateGroupDataMapper.toDbo(data));
  }

  @Override
  @Transactional
  public void sendInvitationToken(
      final GroupId groupId, final UserId userId, final AuthorizedUser requester) {
    groupChecker.verifyGroupExists(groupId);
    groupChecker.verifyUserExists(userId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    }
    if (groupRepository.isUserInGroup(userId, groupId)) {
      throw new UserAlreadyInGroupException();
    }
    if (groupRepository.countMembershipsOfGroup(groupId) >= groupProperties.maxNumUsersPerGroup()) {
      throw new MaximumGroupMembersReachedException();
    }
    if (groupRepository.countMembershipsOfUser(userId) >= groupProperties.maxNumGroupsPerUser()) {
      throw new MaximumGroupsPerUserReachedException();
    }
    if (groupInvitationRepository.countByCreatedBy(requester.id())
        >= groupProperties.maxNumInvitations()) {
      throw new MaximumGroupInvitationsReachedException();
    }
    if (groupInvitationRepository.existsByGroupIdAndUserId(groupId, userId)) {
      throw new UserAlreadyInvitedException();
    }
    final Optional<GroupInvitationEntity> existingInvitation =
        groupInvitationRepository.findByKey(userId, groupId, requester.id());
    final Instant now = Instant.now(clock);
    if (existingInvitation.isPresent()) {
      final GroupInvitationEntity invitation = existingInvitation.get();
      if (invitation.lastSentAt().plus(groupProperties.waitTimeBetweenInvitations()).isAfter(now)) {
        throw new SendGroupInvitationTooSoonException();
      } else {
        groupInvitationRepository.deleteByKey(
            invitation.userId(), invitation.groupId(), invitation.createdBy());
      }
    }
    final GroupInvitationToken token = GroupInvitationToken.generate();
    final ExpirationTime expiresAt =
        ExpirationTime.of(now.plus(groupProperties.invitationDuration()));
    final UserEntity userToInvite =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User to invite not found"));
    final GroupEntity groupToInvite =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new GroupNotFoundException("Group to invite not found"));
    groupInvitationRepository.create(
        new CreateGroupInvitationDbo(groupId, userId, token, requester.id(), expiresAt, now));
    eventPublisher.publishEvent(
        new GroupInvitationRequestedEvent(
            userToInvite.id(),
            userToInvite.email(),
            userToInvite.username(),
            groupToInvite.name(),
            token));
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
    if (!invitationDb.userId().equals(requester.id())) {
      throw new WrongUserForInvitationException();
    }
    groupChecker.verifyGroupExists(invitationDb.groupId());
    if (groupRepository.countMembershipsOfGroup(invitationDb.groupId())
        >= groupProperties.maxNumUsersPerGroup()) {
      throw new MaximumGroupMembersReachedException();
    }
    if (groupRepository.countMembershipsOfUser(requester.id())
        >= groupProperties.maxNumGroupsPerUser()) {
      throw new MaximumGroupsPerUserReachedException();
    }
    groupInvitationRepository.deleteByToken(token);
    groupRepository.addUserToGroup(requester.id(), invitationDb.groupId());
  }

  @Override
  @Transactional
  public List<MemberData> getUsersInGroup(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.verifyGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    }
    return groupRepository.findUsersByGroupId(groupId).stream()
        .map(MemberDataMapper::fromDbo)
        .toList();
  }

  @Override
  @Transactional
  public void addUserToGroupAsAdmin(final GroupId groupId, final UserId userIdToAdd) {
    groupChecker.verifyGroupExists(groupId);
    groupChecker.verifyUserExists(userIdToAdd);
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
  public void removeUserFromGroup(
      final GroupId groupId, final UserId userIdToRemove, final AuthorizedUser requester) {
    groupChecker.verifyGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    }
    groupChecker.verifyUserExists(userIdToRemove);
    groupRepository.removeUserFromGroup(userIdToRemove, groupId);
  }

  @Override
  @Transactional
  public List<BalanceData> getGroupBalances(final GroupId groupId, final AuthorizedUser requester) {
    groupChecker.verifyGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    }
    // TODO
    return List.of();
  }

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public Page<TransactionData> getGroupTransactions(
      final GroupId groupId, final AuthorizedUser requester, final int page, final int size) {
    groupChecker.verifyGroupExists(groupId);
    if (requester.role().equals(Role.USER)) {
      groupChecker.verifyUserIsInGroup(requester.id(), groupId);
    }
    final Pageable pageable = PageRequest.of(page, size);
    final Page<TransactionReference> references =
        transactionRepository.findAllByGroupId(groupId, pageable);
    if (references.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, references.getTotalElements());
    }
    final Currency currency =
        groupRepository
            .findById(groupId)
            .orElseThrow(
                () -> {
                  final String message =
                      "Group with ID '%s' does not exist".formatted(groupId.value());
                  log.info(message);
                  return new GroupNotFoundException(message);
                })
            .currency();
    final Set<ExpenseId> expenseIds =
        references.stream()
            .filter(reference -> reference.type() == TransactionType.EXPENSE)
            .map(reference -> ExpenseId.fromUuid(reference.id()))
            .collect(Collectors.toUnmodifiableSet());
    final Set<PaymentId> paymentIds =
        references.stream()
            .filter(reference -> reference.type() == TransactionType.PAYMENT)
            .map(reference -> PaymentId.fromUuid(reference.id()))
            .collect(Collectors.toUnmodifiableSet());
    final Map<ExpenseId, FullExpenseEntity> expensesById =
        expenseIds.isEmpty()
            ? Map.of()
            : expenseRepository.findAllByGroupIdAndIds(groupId, expenseIds).stream()
                .collect(
                    Collectors.toUnmodifiableMap(
                        entity -> entity.expense().id(), Function.identity()));
    final Map<PaymentId, PaymentEntity> paymentsById =
        paymentIds.isEmpty()
            ? Map.of()
            : paymentRepository.findAllByGroupIdAndIds(groupId, paymentIds).stream()
                .collect(Collectors.toUnmodifiableMap(PaymentEntity::id, Function.identity()));
    return references.map(
        reference ->
            switch (reference.type()) {
              case EXPENSE ->
                  TransactionDataMapper.fromExpense(
                      requireExpense(expensesById, ExpenseId.fromUuid(reference.id())), currency);

              case PAYMENT ->
                  TransactionDataMapper.fromPayment(
                      requirePayment(paymentsById, PaymentId.fromUuid(reference.id())), currency);
            });
  }

  private FullExpenseEntity requireExpense(
      final Map<ExpenseId, FullExpenseEntity> expensesById, final ExpenseId id) {
    final FullExpenseEntity expense = expensesById.get(id);
    if (expense != null) {
      return expense;
    }
    final String message = "Expense with ID '%s' does not exist".formatted(id.value());
    log.warn(message);
    throw new ExpenseNotFoundException(message);
  }

  private PaymentEntity requirePayment(
      final Map<PaymentId, PaymentEntity> paymentsById, final PaymentId id) {
    final PaymentEntity payment = paymentsById.get(id);
    if (payment != null) {
      return payment;
    }
    final String message = "Payment with ID '%s' does not exist".formatted(id.value());
    log.warn(message);
    throw new PaymentNotFoundException(message);
  }

  @Override
  public void forceSyncBalancesAsAdmin(final GroupId groupId) {
    // TODO
  }
}
