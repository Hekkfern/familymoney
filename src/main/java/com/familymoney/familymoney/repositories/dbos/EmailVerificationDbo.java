package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import org.springframework.lang.NonNull;

@Builder
public record EmailVerificationDbo(
    @NonNull UUID id,
    @NonNull UserId userId,
    @NonNull EmailVerificationToken token,
    @NonNull Instant createdAt,
    @NonNull Instant expiresAt) {

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
