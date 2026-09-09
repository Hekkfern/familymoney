package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupInvitationInvalidException;
import com.familymoney.domains.transactions.exceptions.GroupOwnerNotFoundException;
import com.familymoney.domains.transactions.exceptions.MaximumGroupInvitationsReachedException;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.properties.GroupInvitationProperties;
import com.familymoney.utils.UUIDGenerator;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
  private final GroupOperations groupOperations;
  private final Clock clock;
  private final GroupInvitationProperties groupInvitationProperties;

  @Override
  public GroupId createGroup(GroupName name, Description description, CurrencyUnit currency) {
    return groupOperations.createGroup(name, description, currency);
  }

  @Override
  @Transactional
  public GroupId createGroupAndAddCreatorAsMember(
      final GroupName name,
      final Description description,
      final CurrencyUnit currency,
      final UserId createdBy) {
    final GroupId groupId = groupOperations.createGroup(name, description, currency);
    try {
      groupRepository
          .addUser(createdBy, groupId)
          .orElseThrow(
              () -> new DatabaseExecutionException("Unable to assign owner to the new group"));
    } catch (final DataIntegrityViolationException e) {
      throw new GroupOwnerNotFoundException("User does not exist");
    }
    return groupId;
  }

  @Override
  @Transactional
  public void deleteGroup(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    groupOperations.deleteGroup(groupId);
  }

  @Override
  @Transactional
  public void deleteGroupAsAdmin(final GroupId groupId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.deleteGroup(groupId);
  }

  @Override
  @Transactional
  public Page<GroupData> getGroupsByUser(final UserId userId, final Pageable pageable) {
    groupOperations.checkIfUserExists(userId);
    return groupOperations.getGroupsByUser(userId, pageable);
  }

  @Override
  @Transactional
  public GroupData getGroupInfo(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    return groupOperations.getGroupInfo(groupId);
  }

  @Override
  @Transactional
  public GroupData getGroupInfoAsAdmin(final GroupId groupId) {
    groupOperations.checkIfGroupExists(groupId);
    return groupOperations.getGroupInfo(groupId);
  }

  @Override
  @Transactional
  public void updateGroupInfo(
      final GroupId groupId, final UserId userId, final UpdateGroupData data) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    groupOperations.updateGroupInfo(groupId, data);
  }

  @Override
  @Transactional
  public void updateGroupInfoAsAdmin(final GroupId groupId, final UpdateGroupData data) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.updateGroupInfo(groupId, data);
  }

  @Override
  @Transactional
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
  @Transactional
  public List<UserId> getUsersInGroup(final GroupId groupId, final UserId userId) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    return groupOperations.getUsersInGroup(groupId);
  }

  @Override
  @Transactional
  public List<UserId> getUsersInGroupAsAdmin(final GroupId groupId) {
    groupOperations.checkIfGroupExists(groupId);
    return groupOperations.getUsersInGroup(groupId);
  }

  @Override
  @Transactional
  public void addUserToGroupAsAdmin(final GroupId groupId, final UserId userIdToAdd) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserExists(userIdToAdd);
    groupRepository
        .addUser(userIdToAdd, groupId)
        .orElseThrow(() -> new DatabaseExecutionException("Unable to add user to group"));
  }

  @Override
  @Transactional
  public void removeUserFromGroup(
      final GroupId groupId, final UserId userId, final UserId userIdToRemove) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserIsInGroup(userId, groupId);
    groupOperations.removeUserFromGroup(groupId, userIdToRemove);
  }

  @Override
  @Transactional
  public void removeUserFromGroupAsAdmin(final GroupId groupId, final UserId userIdToRemove) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserExists(userIdToRemove);
    groupOperations.removeUserFromGroup(groupId, userIdToRemove);
  }
}
