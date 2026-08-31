package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

public interface GroupInvitationRepository {

  /**
   * Creates a new group invitation record.
   *
   * @param data values to persist for the invitation
   * @return an {@link Optional} containing the created {@link GroupInvitationEntity} when the
   *     insert succeeds; otherwise an empty {@link Optional}
   */
  Optional<GroupInvitationEntity> create(CreateGroupInvitationDto data);

  /**
   * Finds a group invitation by its token.
   *
   * @param token the unique invitation token
   * @return an {@link Optional} containing the matching {@link GroupInvitationEntity} when found;
   *     otherwise an empty {@link Optional}
   */
  Optional<GroupInvitationEntity> findByToken(GroupInvitationToken token);

  /**
   * Deletes a group invitation by its token.
   *
   * @param token the unique invitation token
   * @return {@code true} if a matching invitation was deleted; otherwise {@code false}
   */
  boolean deleteByToken(GroupInvitationToken token);

  /**
   * Counts invitations for the given group and user pair.
   *
   * @param groupId the group identifier
   * @param userId the user identifier
   * @return the number of invitation records that match the provided group and user IDs
   */
  long countByGroupIdAndUserId(GroupId groupId, UserId userId);
}
