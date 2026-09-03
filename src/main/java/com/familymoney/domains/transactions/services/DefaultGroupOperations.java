package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.services.mappers.GroupDataMapper;
import com.familymoney.domains.transactions.services.mappers.UpdateGroupDataMapper;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.exceptions.DatabaseExecutionException;
import java.util.List;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Default implementation of shared transaction group operations. */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupOperations implements GroupOperations {

  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  @Override
  public GroupId createGroup(
      final GroupName name, final Description description, final CurrencyUnit currency) {
    final GroupId groupId = GroupId.generate();
    groupRepository
        .create(new CreateGroupDto(groupId, name, description, currency))
        .orElseThrow(() -> new DatabaseExecutionException("Unable to create group"));
    return groupId;
  }

  @Override
  public void deleteGroup(final GroupId groupId) {
    checkIfGroupExists(groupId);
    groupRepository.deleteById(groupId);
  }

  @Override
  public Page<GroupData> getGroupsByUser(final UserId userId, final Pageable pageable) {
    return groupRepository.findByUserId(userId, pageable).map(GroupDataMapper::fromDbo);
  }

  @Override
  public GroupData getGroupInfo(final GroupId groupId) {
    checkIfGroupExists(groupId);
    return groupRepository
        .findById(groupId)
        .map(GroupDataMapper::fromDbo)
        .orElseThrow(
            () ->
                new TransactionGroupNotFoundException(
                    String.format("Unable to find group %s", groupId)));
  }

  @Override
  public void updateGroupInfo(final GroupId groupId, final UpdateGroupData data) {
    checkIfGroupExists(groupId);
    groupRepository.updateById(groupId, UpdateGroupDataMapper.toDbo(data));
  }

  @Override
  public List<UserId> getUsersInGroup(final GroupId groupId) {
    checkIfGroupExists(groupId);
    return groupRepository.findUserIdsByGroupId(groupId);
  }

  @Override
  public void removeUserFromGroup(final GroupId groupId, final UserId userId) {
    checkIfGroupExists(groupId);
    checkIfUserExists(userId);
    groupRepository.deleteUser(userId, groupId);
  }

  @Override
  public void checkIfGroupExists(final GroupId groupId) {
    final boolean exists = groupRepository.existsById(groupId);
    if (!exists) {
      final String msg = "Group '%s' does not exist".formatted(groupId);
      log.info(msg);
      throw new TransactionGroupNotFoundException(msg);
    }
  }

  @Override
  public void checkIfUserExists(final UserId userId) {
    if (userRepository.existsById(userId)) {
      return;
    }
    final String message = "User '%s' does not exist".formatted(userId);
    log.info(message);
    throw new UserNotFoundException(message);
  }

  @Override
  public void checkIfUserIsInGroup(final UserId userId, final GroupId groupId)
      throws UserIsNotMemberOfGroupException {
    if (!groupRepository.isUserInGroup(userId, groupId)) {
      final String msg = "User '%s' is not a member of group '%s'".formatted(userId, groupId);
      log.info(msg);
      throw new UserIsNotMemberOfGroupException(msg);
    }
  }
}
