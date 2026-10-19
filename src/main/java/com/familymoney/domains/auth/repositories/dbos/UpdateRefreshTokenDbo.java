package com.familymoney.domains.auth.repositories.dbos;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateRefreshTokenDbo(
    @Nullable RefreshToken token, @Nullable ExpirationTime expiresAt) {

  public boolean isEmpty() {
    return token == null && expiresAt == null;
  }
}
