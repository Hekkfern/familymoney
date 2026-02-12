package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.familymoney.repositories.entities.GroupInvitationEntity;
import com.familymoney.familymoney.types.GroupInvitationToken;
import java.time.Duration;
import java.util.Optional;

public interface IGroupInvitationRepository {

  /**
   * Creates a new group invitation record with the specified group ID, invitation token, and
   * expiration time.
   *
   * @param data values to store
   * @return an {@link Optional} containing the created GroupInvitationDbo if the creation was
   *     successful, or an empty {@link Optional} if the creation failed (e.g., due to invalid input
   *     or database constraints)
   */
  Optional<GroupInvitationEntity> create(CreateGroupInvitationDto data);

  /**
   * Retrieves a group invitation record based on the provided invitation token.
   *
   * @param token the unique token associated with the group invitation
   * @return an {@link Optional} containing the found GroupInvitationDbo if a matching record is
   *     found, or an empty {@link Optional} if no matching record exists for the provided token
   */
  Optional<GroupInvitationEntity> findByToken(GroupInvitationToken token);

  /**
   * Deletes a group invitation record based on the provided invitation token.
   *
   * @param token the unique token associated with the group invitation to be deleted
   * @return true if the deletion was successful (i.e., a record was found and deleted), or false if
   *     no matching record exists for the provided token
   */
  boolean deleteByToken(GroupInvitationToken token);

  /**
   * Deletes group invitation records that are older than the specified duration from the current
   * time. This method is typically used to clean up expired invitations that are no longer valid.
   *
   * @param cutoff the duration used to determine which records to delete based on their age.
   *     Records older than the current time minus this duration will be deleted.
   */
  void deleteOlderThan(Duration cutoff);
}
