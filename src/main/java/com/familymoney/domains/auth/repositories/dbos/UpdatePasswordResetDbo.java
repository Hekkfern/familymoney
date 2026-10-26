package com.familymoney.domains.auth.repositories.dbos;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
import jakarta.annotation.Nullable;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UpdatePasswordResetDbo(
    @Nullable PasswordResetToken token,
    @Nullable ExpirationTime expiresAt,
    @Nullable Instant lastSentAt) {

  public UpdatePasswordResetDbo {
    final boolean isEmptyUpdate = token == null && expiresAt == null && lastSentAt == null;
    if (isEmptyUpdate) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
  }

  public boolean isEmpty() {
    return token == null && expiresAt == null && lastSentAt == null;
  }
}
