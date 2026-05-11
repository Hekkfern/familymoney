package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.TokenFamilyBlacklistEntity;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.TokenfamilyBlacklist;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class TokenFamilyBlacklistJooqMapper {

  private TokenFamilyBlacklistJooqMapper() {}

  public static TokenFamilyBlacklistEntity toEntity(final Record r) {
    final OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.EXPIRES_AT));
    final TokenFamily family =
        TokenFamily.fromUuid(
            Objects.requireNonNull(r.get(TokenfamilyBlacklist.TOKENFAMILY_BLACKLIST.FAMILY)));
    return TokenFamilyBlacklistEntity.builder()
        .family(family)
        .expiresAt(expiresAt.toInstant())
        .build();
  }
}
