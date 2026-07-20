package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.RefreshToken;
import java.time.Instant;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateRefreshTokenDto(@Nullable RefreshToken token, @Nullable Instant expiresAt) {

  public boolean isEmpty() {
    return token == null && expiresAt == null;
  }
}
