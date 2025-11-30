package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import org.springframework.lang.NonNull;

@Builder
public record RefreshTokenDbo(
    @NonNull UUID id,
    @NonNull UserId userId,
    @NonNull RefreshToken token,
    @NonNull Instant createdAt,
    @NonNull Instant expiresAt,
    boolean isUsed,
    @NonNull Optional<Instant> usedAt,
    @NonNull UUID family) {

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return !isUsed && !isExpired();
  }
}
