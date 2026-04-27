package com.familymoney.repositories;

import com.familymoney.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.repositories.entities.RefreshTokenEntity;
import com.familymoney.types.RefreshToken;
import com.familymoney.types.UserId;
import java.util.Optional;
import java.util.UUID;

/** Repository contract for managing refresh tokens used for session renewal. */
public interface IRefreshTokenRepository {

  /**
   * Create and persist a new refresh token associated with a user.
   *
   * @param data values to store.
   * @return an {@link Optional} containing the persisted {@link RefreshTokenEntity} when creation
   *     succeeds, or an empty Optional when creation fails (e.g. due to constraint violation).
   */
  Optional<RefreshTokenEntity> create(CreateRefreshTokenDto data);

  /**
   * Find a refresh token record by its token value.
   *
   * @param token the refresh token to query. Must not be null.
   * @return an {@link Optional} containing the {@link RefreshTokenEntity} when found, otherwise empty.
   */
  Optional<RefreshTokenEntity> findByToken(RefreshToken token);

  /**
   * Update fields of a refresh token identified by its token value.
   *
   * @param token the token to update. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   * @return true if at least one row was updated; false if no matching token exists or no change
   *     was performed.
   */
  boolean updateByToken(RefreshToken token, UpdateRefreshTokenDto data);

  /**
   * Update refresh tokens that belong to a given family UUID.
   *
   * @param family the family UUID whose tokens should be updated. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   * @return true if at least one row was updated; false if no tokens matched the provided family or
   *     no change occurred.
   */
  boolean updateByFamily(UUID family, UpdateRefreshTokenDto data);

  /**
   * Update refresh tokens associated with a specific user id.
   *
   * @param userId the id of the user whose tokens should be updated. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   * @return true if at least one row was updated; false otherwise.
   */
  boolean updateByUserId(UserId userId, UpdateRefreshTokenDto data);
}
