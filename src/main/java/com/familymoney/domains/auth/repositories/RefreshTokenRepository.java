package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dbos.CreateRefreshTokenDbo;
import com.familymoney.domains.auth.repositories.dbos.UpdateRefreshTokenDbo;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

/** Repository contract for managing refresh tokens used for session renewal. */
public interface RefreshTokenRepository {

  /**
   * Create and persist a new refresh token associated with a user.
   *
   * @param data values to store.
   */
  void create(CreateRefreshTokenDbo data);

  /**
   * Find a refresh token record by its token value.
   *
   * @param token the refresh token to query. Must not be null.
   * @return an {@link Optional} containing the {@link RefreshTokenEntity} when found, otherwise
   *     empty.
   */
  Optional<RefreshTokenEntity> findByToken(RefreshToken token);

  /**
   * Update fields of a refresh token identified by its token value.
   *
   * @param token the token to update. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   */
  void updateByToken(RefreshToken token, UpdateRefreshTokenDbo data);

  /**
   * Update refresh tokens associated with a specific user id.
   *
   * @param userId the id of the user whose tokens should be updated. Must not be null.
   * @param data the update payload containing fields to change. Must not be null. Only non-null
   *     fields in {@code data} will be modified.
   */
  void updateByUserId(UserId userId, UpdateRefreshTokenDbo data);

  /**
   * Delete refresh token records by their token value.
   *
   * @param token the token to delete. Must not be null.
   */
  void deleteByToken(RefreshToken token);

  /**
   * Deletes all refresh tokens belonging to a user.
   *
   * @param userId identifier of the user whose refresh tokens are deleted
   */
  void deleteByUserId(UserId userId);
}
