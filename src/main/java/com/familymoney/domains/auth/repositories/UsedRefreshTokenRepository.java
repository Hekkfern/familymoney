package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateUsedRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.repositories.mappers.UsedRefreshTokenJooqMapper;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.generated.tables.UsedRefreshTokens;
import com.familymoney.security.IOpaqueTokenHasher;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsedRefreshTokenRepository implements IUsedRefreshTokenRepository {

  private final DSLContext db;
  private final IOpaqueTokenHasher tokenHasher;

  @Override
  public Optional<UsedRefreshTokenEntity> create(final CreateUsedRefreshTokenDto data) {
    return db.insertInto(UsedRefreshTokens.USED_REFRESH_TOKENS)
        .columns(
            UsedRefreshTokens.USED_REFRESH_TOKENS.TOKEN_HASH,
            UsedRefreshTokens.USED_REFRESH_TOKENS.FAMILY,
            UsedRefreshTokens.USED_REFRESH_TOKENS.USED_AT)
        .values(
            tokenHasher.hash(data.token().value()),
            data.family().value(),
            data.usedAt().atOffset(ZoneOffset.UTC))
        .onConflictDoNothing()
        .returning(
            UsedRefreshTokens.USED_REFRESH_TOKENS.FAMILY,
            UsedRefreshTokens.USED_REFRESH_TOKENS.USED_AT,
            UsedRefreshTokens.USED_REFRESH_TOKENS.CREATED_AT)
        .fetchOptional()
        .map(UsedRefreshTokenJooqMapper::toEntity);
  }

  @Override
  public Optional<UsedRefreshTokenEntity> findByToken(final RefreshToken token) {
    return db.select(
            UsedRefreshTokens.USED_REFRESH_TOKENS.FAMILY,
            UsedRefreshTokens.USED_REFRESH_TOKENS.USED_AT,
            UsedRefreshTokens.USED_REFRESH_TOKENS.CREATED_AT)
        .from(UsedRefreshTokens.USED_REFRESH_TOKENS)
        .where(UsedRefreshTokens.USED_REFRESH_TOKENS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
        .fetchOptional()
        .map(UsedRefreshTokenJooqMapper::toEntity);
  }
}
