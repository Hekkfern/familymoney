package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.RefreshTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class RefreshTokenJooqMapper {

  private RefreshTokenJooqMapper() {}

  public static RefreshTokenEntity toEntity(final Record r) {
    OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.CREATED_AT));
    OffsetDateTime updatedAt =
        Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.UPDATED_AT));
    OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.EXPIRES_AT));
    final TokenFamily family =
        TokenFamily.fromUuid(Objects.requireNonNull(r.get(RefreshTokens.REFRESH_TOKENS.FAMILY)));

    return RefreshTokenEntity.builder()
        .id(r.get(RefreshTokens.REFRESH_TOKENS.ID))
        .userId(UserId.fromUuid(r.get(RefreshTokens.REFRESH_TOKENS.USER_ID)))
        .token(RefreshToken.fromString(r.get(RefreshTokens.REFRESH_TOKENS.TOKEN)))
        .createdAt(createdAt.toInstant())
        .updatedAt(updatedAt.toInstant())
        .expiresAt(expiresAt.toInstant())
        .family(family)
        .build();
  }
}
