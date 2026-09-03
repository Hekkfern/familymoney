package com.familymoney.domains.admin.services;

import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.services.GroupOperations;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
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

/** Default administration service for transaction groups. */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultTransactionGroupAdminService implements TransactionGroupAdminService {

  private final GroupOperations groupOperations;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  @Override
  public GroupId createGroup(
      final GroupName name, final Description description, final CurrencyUnit currency) {
    return groupOperations.createGroup(name, description, currency);
  }

  @Override
  public void deleteGroup(final GroupId groupId) {
    groupOperations.deleteGroup(groupId);
  }

  @Override
  public Page<GroupData> getGroupsByUser(final UserId userId, final Pageable pageable) {
    return groupOperations.getGroupsByUser(userId, pageable);
  }

  @Override
  public GroupData getGroupInfo(final GroupId groupId) {
    return groupOperations.getGroupInfo(groupId);
  }

  @Override
  public void updateGroupInfo(final GroupId groupId, final UpdateGroupData data) {
    groupOperations.updateGroupInfo(groupId, data);
  }

  @Override
  public List<UserId> getUsersInGroup(final GroupId groupId) {
    return groupOperations.getUsersInGroup(groupId);
  }

  @Override
  public void addUserToGroup(final GroupId groupId, final UserId userIdToAdd) {
    groupOperations.checkIfGroupExists(groupId);
    groupOperations.checkIfUserExists(userIdToAdd);
    groupRepository
        .addUser(userIdToAdd, groupId)
        .orElseThrow(() -> new DatabaseExecutionException("Unable to add user to group"));
  }

  @Override
  public void removeUserFromGroup(final GroupId groupId, final UserId userIdToRemove) {
    groupOperations.removeUserFromGroup(groupId, userIdToRemove);
  }
}
