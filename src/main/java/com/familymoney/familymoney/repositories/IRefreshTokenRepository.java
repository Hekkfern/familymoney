package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateRefreshTokenDbo;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/** Repository contract for managing refresh tokens used for session renewal. */
public interface IRefreshTokenRepository {

  /**
   * Create and persist a new refresh token associated with a user.
   *
   * @param userId ID of the user the token belongs to. Must not be null.
   * @param token the refresh token value to persist. Must not be null.
   * @param family UUID representing a token family. Tokens that belong to the same family can be
   *     managed/rotated/revoked together. Must not be null.
   * @return an {@link Optional} containing the persisted {@link RefreshTokenDbo} when creation
   *     succeeds, or an empty Optional when creation fails (e.g. due to constraint violation).
   */
  Optional<RefreshTokenDbo> create(UserId userId, RefreshToken token, UUID family);

  /**
   * Find a refresh token record by its token value.
   *
   * @param token the refresh token to query. Must not be null.
   * @return an {@link Optional} containing the {@link RefreshTokenDbo} when found, otherwise empty.
   */
  Optional<RefreshTokenDbo> findByToken(RefreshToken token);

  /**
   * Update fields of a refresh token identified by its token value.
   *
   * @param token the token to update. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   * @return true if at least one row was updated; false if no matching token exists or no change
   *     was performed.
   */
  boolean updateByToken(RefreshToken token, UpdateRefreshTokenDbo data);

  /**
   * Update refresh tokens that belong to a given family UUID.
   *
   * @param family the family UUID whose tokens should be updated. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   * @return true if at least one row was updated; false if no tokens matched the provided family or
   *     no change occurred.
   */
  boolean updateByFamily(UUID family, UpdateRefreshTokenDbo data);

  /**
   * Update refresh tokens associated with a specific user id.
   *
   * @param userId the id of the user whose tokens should be updated. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   * @return true if at least one row was updated; false otherwise.
   */
  boolean updateByUserId(UserId userId, UpdateRefreshTokenDbo data);
}
