package com.familymoney.repositories;

import com.familymoney.repositories.dtos.CreateGroupDto;
import com.familymoney.repositories.dtos.UpdateGroupDto;
import com.familymoney.repositories.entities.GroupEntity;
import com.familymoney.repositories.entities.UserGroupEntity;
import com.familymoney.types.GroupId;
import com.familymoney.types.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface that defines persistence operations for transaction groups.
 *
 * <p>Implementations of this interface are responsible for creating, updating and deleting groups,
 * resolving membership, and paging queries for groups associated with a user.
 *
 * <p>All methods return Domain-specific DB objects (Dbo) or primitives that indicate success.
 * Optional is used for methods that may not find or create a resource.
 */
public interface IGroupRepository {

  /**
   * Create a new group for transactions.
   *
   * @param data values to store
   * @return Optional containing the created GroupDbo when creation succeeds; empty Optional when
   *     creation fails (for example because of a constraint violation).
   */
  Optional<GroupEntity> create(CreateGroupDto data);

  /**
   * Update group data by its ID. Only non-null fields of {@code data} should be applied.
   *
   * @param id ID of the group to update. Must not be null.
   * @param data Data to update. Only non-null fields will be updated.
   * @return true if the group was updated (record existed and changes were applied), false
   *     otherwise.
   */
  boolean updateById(GroupId id, UpdateGroupDto data);

  /**
   * Delete a group by its ID.
   *
   * @param id ID of the group to delete. Must not be null.
   * @return true if the group was deleted, false if no group with the given id existed or the
   *     deletion failed.
   */
  boolean deleteById(GroupId id);

  /**
   * Find the groups that a given user belongs to, returned as a paged result.
   *
   * @param userId ID of the user whose groups are requested. Must not be null.
   * @param pageable Paging information (page number, size, sort). Must not be null.
   * @return A page of GroupDbo objects representing groups the user is a member of. If the user has
   *     no groups the returned page will be empty.
   */
  Page<GroupEntity> findByUserId(UserId userId, Pageable pageable);

  /**
   * Find a group by its identifier.
   *
   * @param id ID of the group to retrieve. Must not be null.
   * @return Optional containing the GroupDbo if found, otherwise empty.
   */
  Optional<GroupEntity> findById(GroupId id);

  /**
   * Check whether a group with the given id exists.
   *
   * @param id ID of the group to check. Must not be null.
   * @return true if the group exists, false otherwise.
   */
  boolean existsById(GroupId id);

  /**
   * Return the list of user ids that are members of the provided group.
   *
   * @param id ID of the group. Must not be null.
   * @return List of {@link UserId} for users in the group. The list will be empty if there are no
   *     members or the group does not exist.
   */
  List<UserId> findUserIdsByGroupId(GroupId id);

  /**
   * Check whether a given user is part of the given group.
   *
   * @param userId ID of the user to check. Must not be null.
   * @param groupId ID of the group to check. Must not be null.
   * @return true if the user is a member of the group, false otherwise.
   */
  boolean isUserInGroup(UserId userId, GroupId groupId);

  /**
   * Add a user to a group.
   *
   * @param userId ID of the user to add. Must not be null.
   * @param groupId ID of the group to which the user should be added. Must not be null.
   * @return {@link Optional} containing the created {@link UserGroupEntity} when the user was added
   *     successfully, or empty {@link Optional} when the operation failed (for example if the user
   *     or group doesn't exist or the user is already a member).
   */
  Optional<UserGroupEntity> addUser(UserId userId, GroupId groupId);

  /**
   * Remove a user from a group.
   *
   * @param userId ID of the user to remove. Must not be null.
   * @param groupId ID of the group from which the user should be removed. Must not be null.
   * @return true if the user was removed (membership existed and was deleted), false otherwise.
   */
  boolean deleteUser(UserId userId, GroupId groupId);
}
