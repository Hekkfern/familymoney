package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dbos.CreateGroupInvitationDbo;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.Optional;

public interface GroupInvitationRepository {

  /**
   * Creates a new group invitation record.
   *
   * @param data values to persist for the invitation
   */
  void create(CreateGroupInvitationDbo data);

  /**
   * Finds a group invitation by its token.
   *
   * @param token the unique invitation token
   * @return an {@link Optional} containing the matching {@link GroupInvitationEntity} when found;
   *     otherwise an empty {@link Optional}
   */
  Optional<GroupInvitationEntity> findByToken(GroupInvitationToken token);

  /**
   * Finds a group invitation by user ID, group ID, and the ID of the user who created the
   * invitation.
   *
   * @param userId the user identifier
   * @param groupId the group identifier
   * @param createdBy the identifier of the user who created the invitation
   * @return an {@link Optional} containing the matching {@link GroupInvitationEntity} when found;
   *     otherwise an empty {@link Optional}
   */
  Optional<GroupInvitationEntity> findByKey(UserId userId, GroupId groupId, UserId createdBy);

  /**
   * Deletes a group invitation by its token.
   *
   * @param token the unique invitation token
   */
  void deleteByToken(GroupInvitationToken token);

  /**
   * Deletes a group invitation by user ID, group ID, and the ID of the user who created the
   * invitation.
   *
   * @param userId the user identifier
   * @param groupId the group identifier
   * @param createdBy the identifier of the user who created the invitation
   */
  void deleteByKey(UserId userId, GroupId groupId, UserId createdBy);

  /**
   * Counts the number of non-expired group invitations created by a specific user.
   *
   * @param createdBy the identifier of the user who created the invitations
   * @return the count of invitations created by the specified user
   */
  int countByCreatedBy(UserId createdBy);

  /**
   * Checks if a non-expired group invitation exists for the given group ID and user ID .
   *
   * @param groupId the group identifier
   * @param userId the user identifier
   * @return {@code true} if a non-expired invitation exists; {@code false} otherwise
   */
  boolean existsByGroupIdAndUserId(GroupId groupId, UserId userId);

  /**
   * Deletes a batch of invitations that belong to groups soft-deleted before the given instant.
   *
   * @param deletedBefore only invitations of groups soft-deleted before this instant are deleted
   * @param batchSize the maximum number of invitations to delete
   * @return the number of deleted invitations
   */
  int deleteInvitationsOfPurgeableGroups(Instant deletedBefore, int batchSize);
}
