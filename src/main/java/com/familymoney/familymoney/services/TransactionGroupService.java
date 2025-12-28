package com.familymoney.familymoney.services;

import com.familymoney.familymoney.exceptions.DatabaseExecutionException;
import com.familymoney.familymoney.exceptions.GroupNotOwnedByUserException;
import com.familymoney.familymoney.repositories.IBalanceRepository;
import com.familymoney.familymoney.repositories.IGroupRepository;
import com.familymoney.familymoney.repositories.ITransactionRepository;
import com.familymoney.familymoney.services.data.GetGroupData;
import com.familymoney.familymoney.services.mappers.GetGroupDataMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionGroupService implements ITransactionGroupService {

  private final IGroupRepository groupRepository;
  private final IBalanceRepository balanceRepository;
  private final ITransactionRepository transactionRepository;
  private final GetGroupDataMapper getGroupDataMapper;

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

  @Override
  public void deleteGroupOwnedBy(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    if (!groupRepository.isUserInGroup(userId, groupId)) {
      throw new GroupNotOwnedByUserException(
          String.format("User %s is not a member of group %s", userId, groupId));
    }
    // Delete group
    groupRepository.deleteById(groupId);
  }

  @Override
  public GetGroupData getGroupInfoOwnedBy(GroupId groupId, UserId userId) {
    // Check if the user is a member of the group
    if (!groupRepository.isUserInGroup(userId, groupId)) {
      throw new GroupNotOwnedByUserException(
          String.format("User %s is not a member of group %s", userId, groupId));
    }
    // Get data
    return groupRepository
        .findById(groupId)
        .map(getGroupDataMapper::fromDbo)
        .orElseThrow(
            () ->
                new DatabaseExecutionException(String.format("Unable to find group %s", groupId)));
  }

  @Override
  public boolean isUserInGroup(UserId userId, GroupId groupId) {
    return groupRepository.isUserInGroup(userId, groupId);
  }
}
