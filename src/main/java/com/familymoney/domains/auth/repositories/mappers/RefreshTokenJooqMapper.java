package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.RefreshTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class RefreshTokenJooqMapper {

  private RefreshTokenJooqMapper() {
    /* this class is not meant to be instantiated */
  }

  public static RefreshTokenEntity toEntity(final Record r) {
    final OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.CREATED_AT));
    final OffsetDateTime updatedAt =
        Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.UPDATED_AT));
    final OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.EXPIRES_AT));
    final TokenFamily family =
        TokenFamily.fromUuid(Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.FAMILY)));

    return new RefreshTokenEntity(
        r.get(RefreshTokens.REFRESH_TOKENS.ID),
        UserId.fromUuid(r.get(RefreshTokens.REFRESH_TOKENS.USER_ID)),
        RefreshToken.fromString(r.get(RefreshTokens.REFRESH_TOKENS.TOKEN)),
        createdAt.toInstant(),
        updatedAt.toInstant(),
        ExpirationTime.of(expiresAt),
        family);
  }
}
