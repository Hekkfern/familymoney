package com.familymoney.repositories.mappers;

import com.familymoney.generated.tables.RefreshTokens;
import com.familymoney.repositories.entities.RefreshTokenEntity;
import com.familymoney.types.RefreshToken;
import com.familymoney.types.UserId;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.jooq.Record;

public final class RefreshTokenJooqMapper {

  private RefreshTokenJooqMapper() {}

  public static RefreshTokenEntity toEntity(final Record r) {
    OffsetDateTime createdAt = r.get(RefreshTokens.REFRESH_TOKENS.CREATED_AT);
    OffsetDateTime expiresAt = r.get(RefreshTokens.REFRESH_TOKENS.EXPIRES_AT);
    OffsetDateTime usedAt = r.get(RefreshTokens.REFRESH_TOKENS.USED_AT);

    return RefreshTokenEntity.builder()
        .id(r.get(RefreshTokens.REFRESH_TOKENS.ID))
        .userId(UserId.fromUuid(r.get(RefreshTokens.REFRESH_TOKENS.USER_ID)))
        .token(RefreshToken.fromString(r.get(RefreshTokens.REFRESH_TOKENS.TOKEN)))
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .expiresAt(expiresAt != null ? expiresAt.toInstant() : null)
        .isUsed(Boolean.TRUE.equals(r.get(RefreshTokens.REFRESH_TOKENS.IS_USED)))
        .usedAt(Optional.ofNullable(usedAt).map(OffsetDateTime::toInstant))
        .family(r.get(RefreshTokens.REFRESH_TOKENS.FAMILY))
        .build();
  }
}
