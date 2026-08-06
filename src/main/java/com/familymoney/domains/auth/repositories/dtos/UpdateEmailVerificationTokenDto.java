package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.ExpirationTime;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateEmailVerificationTokenDto(
    @Nullable EmailVerificationToken token,
    @Nullable ExpirationTime expiresAt,
    @Nullable Instant lastSentAt) {

  public boolean isEmpty() {
    return token == null && expiresAt == null && lastSentAt == null;
  }
}
