package com.familymoney.domains.admin.services;

import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import java.util.List;
import javax.money.CurrencyUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Provides administrative operations for transaction groups. */
public interface TransactionGroupAdminService {

  /**
   * Creates a transaction group.
   *
   * @param name the group name
   * @param description the group description
   * @param currency the currency for group transactions
   * @return the identifier of the created group
   */
  GroupId createGroup(GroupName name, Description description, CurrencyUnit currency);

  /**
   * Deletes a transaction group.
   *
   * @param groupId the identifier of the group to delete
   */
  void deleteGroup(GroupId groupId);

  /**
   * Gets a page of transaction groups for a user.
   *
   * @param userId the identifier of the user
   * @param pageable the page request
   * @return a page of transaction group data
   */
  Page<GroupData> getGroupsByUser(UserId userId, Pageable pageable);

  /**
   * Gets transaction group information.
   *
   * @param groupId the identifier of the group
   * @return the transaction group data
   */
  GroupData getGroupInfo(GroupId groupId);

  /**
   * Updates transaction group information.
   *
   * @param groupId the identifier of the group to update
   * @param data the group data to update
   */
  void updateGroupInfo(GroupId groupId, UpdateGroupData data);

  /**
   * Gets the users in a transaction group.
   *
   * @param groupId the identifier of the group
   * @return the identifiers of the users in the group
   */
  List<UserId> getUsersInGroup(GroupId groupId);

  /**
   * Adds a user to a transaction group.
   *
   * @param groupId the identifier of the group
   * @param userIdToAdd the identifier of the user to add
   */
  void addUserToGroup(GroupId groupId, UserId userIdToAdd);

  /**
   * Removes a user from a transaction group.
   *
   * @param groupId the identifier of the group
   * @param userIdToRemove the identifier of the user to remove
   */
  void removeUserFromGroup(GroupId groupId, UserId userIdToRemove);
}
