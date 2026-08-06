package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.UsedRefreshTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class UsedRefreshTokenJooqMapper {

  private UsedRefreshTokenJooqMapper() {}

  public static UsedRefreshTokenEntity toEntity(final Record record) {
    final RefreshToken token =
        RefreshToken.fromString(
            Objects.requireNonNull(record.get(UsedRefreshTokens.USED_REFRESH_TOKENS.TOKEN)));
    final TokenFamily family =
        TokenFamily.fromUuid(
            Objects.requireNonNull(record.get(UsedRefreshTokens.USED_REFRESH_TOKENS.FAMILY)));
    final OffsetDateTime usedAt =
        Objects.requireNonNull(record.get(UsedRefreshTokens.USED_REFRESH_TOKENS.USED_AT));
    final OffsetDateTime createdAt =
        Objects.requireNonNull(record.get(UsedRefreshTokens.USED_REFRESH_TOKENS.CREATED_AT));
    return new UsedRefreshTokenEntity(token, family, usedAt.toInstant(), createdAt.toInstant());
  }
}
