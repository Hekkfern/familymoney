package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository {

  Optional<RefreshTokenDbo> create(UserId userId, RefreshToken token, UUID family);

  Optional<RefreshTokenDbo> findByToken(RefreshToken token);

  void markTokenAsUsed(RefreshToken token);

  void invalidateByFamily(UUID family);

  void invalidateByUserId(UserId userId);

  void deleteOlderThan(Duration cutoff);
}
