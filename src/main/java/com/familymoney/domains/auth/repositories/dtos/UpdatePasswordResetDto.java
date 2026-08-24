package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
import jakarta.annotation.Nullable;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UpdatePasswordResetDto(
    @Nullable PasswordResetToken token,
    @Nullable ExpirationTime expiresAt,
    @Nullable Instant lastSentAt) {

  public boolean isEmpty() {
    return token == null && expiresAt == null && lastSentAt == null;
  }
}
