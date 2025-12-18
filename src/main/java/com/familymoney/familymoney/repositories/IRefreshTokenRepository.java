package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateRefreshTokenDbo;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository {

  /**
   * Create a new refresh token record
   *
   * @param userId ID of the user
   * @param token Refresh token
   * @param family UUID representing the family of tokens
   * @return Created RefreshTokenDbo wrapped in Optional, or empty Optional if creation failed
   */
  Optional<RefreshTokenDbo> create(UserId userId, RefreshToken token, UUID family);

  /**
   * Find a Refresh record by its token
   *
   * @param token Refresh token
   * @return Found RefreshTokenDbo wrapped in Optional, or empty Optional if not found
   */
  Optional<RefreshTokenDbo> findByToken(RefreshToken token);

  boolean updateByToken(RefreshToken token, UpdateRefreshTokenDbo data);

  boolean updateByFamily(UUID family, UpdateRefreshTokenDbo data);

  boolean updateByUserId(UserId userId, UpdateRefreshTokenDbo data);

  /**
   * Delete refresh tokens older than the specified duration from the current time
   *
   * @param cutoff Duration to determine the age of records to delete
   */
  void deleteOlderThan(Duration cutoff);
}
