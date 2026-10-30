package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.UsedRefreshTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class UsedRefreshTokenJooqMapper {

  private UsedRefreshTokenJooqMapper() {}

  public static UsedRefreshTokenEntity toEntity(final Record r) {
    final TokenFamily family =
        TokenFamily.fromUuid(
            Objects.requireNonNull(r.get(UsedRefreshTokens.USED_REFRESH_TOKENS.FAMILY)));
    final OffsetDateTime usedAt =
        Objects.requireNonNull(r.get(UsedRefreshTokens.USED_REFRESH_TOKENS.USED_AT));
    return new UsedRefreshTokenEntity(family, usedAt.toInstant());
  }
}
