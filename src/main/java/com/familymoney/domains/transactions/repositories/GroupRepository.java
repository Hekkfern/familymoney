package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserAlreadyInGroupException;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
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
   * Creates a new transaction group using the identifier, name, description, and currency supplied
   * in {@code dto}. The group starts with no members; callers are responsible for adding the
   * creator through {@link #addUserToGroup(UserId, GroupId)}.
   *
   * @param dto values to persist for the new group
   * @throws UserNotFoundException if the user referenced by {@code dto.createdBy()} does not exist
   */
  void create(CreateGroupDto dto);

  /**
   * Updates a group identified by its ID.
   *
   * <p>Only non-null fields in {@code dto} are applied.
   *
   * @param id the group identifier
   * @param dto the fields to update
   * @throws GroupNotFoundException if no group with the given ID exists
   */
  void updateById(GroupId id, UpdateGroupDto dto);

  /**
   * Soft-deletes a group by its ID. The group is marked as deleted and becomes invisible to every
   * query of this repository. The group row and its dependent rows are physically removed later by
   * the batched purge (see {@link #deletePurgeableGroups(Instant, int)}).
   *
   * @param id the group identifier
   */
  void softDeleteById(GroupId id);

  /**
   * Deletes a batch of group memberships that belong to groups soft-deleted before the given
   * instant.
   *
   * @param deletedBefore only memberships of groups soft-deleted before this instant are deleted
   * @param batchSize the maximum number of memberships to delete
   * @return the number of deleted memberships
   */
  int deleteMembershipsOfPurgeableGroups(Instant deletedBefore, int batchSize);

  /**
   * Deletes a batch of groups soft-deleted before the given instant that no longer have dependent
   * rows (expenses, payments, balances, invitations, or memberships).
   *
   * @param deletedBefore only groups soft-deleted before this instant are deleted
   * @param batchSize the maximum number of groups to delete
   * @return the number of deleted groups
   */
  int deletePurgeableGroups(Instant deletedBefore, int batchSize);

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
   * Returns the user IDs that are members of the given group, ordered by the time each user joined,
   * earliest first.
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
   * @throws UserAlreadyInGroupException if the user is already a member of the group
   * @throws UserNotFoundException if no user with the given ID exists
   * @throws GroupNotFoundException if no group with the given ID exists
   */
  void addUserToGroup(UserId userId, GroupId groupId);
}
