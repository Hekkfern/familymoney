package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateUsedRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.types.RefreshToken;
import java.util.Optional;

/** Repository contract for storing refresh tokens that have been consumed by rotation. */
public interface UsedRefreshTokenRepository {

  /**
   * Persist a consumed refresh token unless it has already been recorded.
   *
   * @param data the data to store.
   * @return the persisted token record, or empty when the token was already present.
   */
  Optional<UsedRefreshTokenEntity> create(CreateUsedRefreshTokenDto data);

  /**
   * Find a consumed refresh token by its value.
   *
   * @param token the consumed refresh token to find.
   * @return the consumed token record when it exists, or empty otherwise.
   */
  Optional<UsedRefreshTokenEntity> findByToken(RefreshToken token);
}
