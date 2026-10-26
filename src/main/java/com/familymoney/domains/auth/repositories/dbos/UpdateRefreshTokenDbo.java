package com.familymoney.domains.auth.repositories.dbos;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record UpdateRefreshTokenDbo(
    @Nullable RefreshToken token, @Nullable ExpirationTime expiresAt) {

  public UpdateRefreshTokenDbo {
    final boolean isEmptyUpdate = token == null && expiresAt == null;
    if (isEmptyUpdate) {
      throw new IllegalArgumentException("At least one field must be provided for update");
    }
  }

  public boolean isEmpty() {
    return token == null && expiresAt == null;
  }
}
