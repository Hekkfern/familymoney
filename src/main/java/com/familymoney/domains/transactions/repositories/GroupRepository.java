package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository contract for persistence operations related to transaction groups.
 *
 * <p>Implementations are responsible for creating, updating, deleting, and querying groups, as well
 * as managing group membership and paged lookups for a user's groups.
 *
 * <p>Methods return domain entities, primitive success flags, or {@link Optional} values when a
 * result may be absent.
 */
public interface GroupRepository {

  /**
   * Creates a new transaction group.
   *
   * @param data values to persist for the new group
   * @return an {@link Optional} containing the created {@link GroupEntity} when creation succeeds;
   *     otherwise an empty {@link Optional}
   */
  Optional<GroupEntity> create(CreateGroupDto data);

  /**
   * Updates a group identified by its ID.
   *
   * <p>Only non-null fields in {@code data} should be applied.
   *
   * @param id the group identifier
   * @param data the fields to update
   * @return {@code true} if the group existed and was updated; {@code false} otherwise
   */
  boolean updateById(GroupId id, UpdateGroupDto data);

  /**
   * Deletes a group by its ID.
   *
   * @param id the group identifier
   * @return {@code true} if the group was deleted; {@code false} if it did not exist or the
   *     deletion failed
   */
  boolean deleteById(GroupId id);

  /**
   * Finds the groups that a given user belongs to as a paged result.
   *
   * @param userId the user identifier
   * @param pageable paging information such as page number, size, and sort
   * @return a page of {@link GroupEntity} objects representing the user's groups; empty if the user
   *     has no groups
   */
  Page<GroupEntity> findByUserId(UserId userId, Pageable pageable);

  /**
   * Finds a group by its identifier.
   *
   * @param id the group identifier
   * @return an {@link Optional} containing the {@link GroupEntity} if found; otherwise empty
   */
  Optional<GroupEntity> findById(GroupId id);

  /**
   * Checks whether a group with the given ID exists.
   *
   * @param id the group identifier
   * @return {@code true} if the group exists; otherwise {@code false}
   */
  boolean existsById(GroupId id);

  /**
   * Returns the user IDs that are members of the given group.
   *
   * @param id the group identifier
   * @return a list of {@link UserId} values for users in the group; empty if the group has no
   *     members or does not exist
   */
  List<UserId> findUserIdsByGroupId(GroupId id);

  /**
   * Checks whether a given user is part of a group.
   *
   * @param userId the user identifier
   * @param groupId the group identifier
   * @return {@code true} if the user is a member of the group; otherwise {@code false}
   */
  boolean isUserInGroup(UserId userId, GroupId groupId);

  /**
   * Adds a user to a group.
   *
   * @param userId the user identifier
   * @param groupId the group identifier
   * @return an {@link Optional} containing the created {@link UserGroupEntity} when the user was
   *     added successfully; otherwise empty
   */
  Optional<UserGroupEntity> addUser(UserId userId, GroupId groupId);

  /**
   * Removes a user from a group.
   *
   * @param userId the user identifier
   * @param groupId the group identifier
   * @return {@code true} if the user membership was removed; otherwise {@code false}
   */
  boolean deleteUser(UserId userId, GroupId groupId);
}
