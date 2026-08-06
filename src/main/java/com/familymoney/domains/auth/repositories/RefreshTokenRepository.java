package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.repositories.mappers.RefreshTokenJooqMapper;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.RefreshTokens;
import com.familymoney.security.IOpaqueTokenHasher;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository implements IRefreshTokenRepository {

  private final DSLContext db;
  private final IOpaqueTokenHasher tokenHasher;

  @Override
  public Optional<RefreshTokenEntity> create(final CreateRefreshTokenDto data) {
    return db.insertInto(RefreshTokens.REFRESH_TOKENS)
        .columns(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN_HASH,
            RefreshTokens.REFRESH_TOKENS.FAMILY,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT)
        .values(
            data.id(),
            data.userId().value(),
            tokenHasher.hash(data.token().value()),
            data.family().value(),
            data.expiresAt().toOffsetDateTime())
        .returning(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.UPDATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .fetchOptional()
        .map(RefreshTokenJooqMapper::toEntity);
  }

  @Override
  public Optional<RefreshTokenEntity> findByToken(final RefreshToken token) {
    return db.select(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.UPDATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .from(RefreshTokens.REFRESH_TOKENS)
        .where(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
        .fetchOptional()
        .map(RefreshTokenJooqMapper::toEntity);
  }

  @Override
  public boolean updateByToken(final RefreshToken token, final UpdateRefreshTokenDto data) {
    final OffsetDateTime expiresAtVal =
        data.expiresAt() != null ? data.expiresAt().toOffsetDateTime() : null;
    final int rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.TOKEN_HASH,
                DSL.coalesce(
                    DSL.val(data.token() != null ? tokenHasher.hash(data.token().value()) : null),
                    RefreshTokens.REFRESH_TOKENS.TOKEN_HASH))
            .set(
                RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
                DSL.coalesce(DSL.val(expiresAtVal), RefreshTokens.REFRESH_TOKENS.EXPIRES_AT))
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean updateByUserId(final UserId userId, final UpdateRefreshTokenDto data) {
    final OffsetDateTime expiresAtVal =
        data.expiresAt() != null ? data.expiresAt().toOffsetDateTime() : null;
    final int rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.TOKEN_HASH,
                DSL.coalesce(
                    DSL.val(data.token() != null ? tokenHasher.hash(data.token().value()) : null),
                    RefreshTokens.REFRESH_TOKENS.TOKEN_HASH))
            .set(
                RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
                DSL.coalesce(DSL.val(expiresAtVal), RefreshTokens.REFRESH_TOKENS.EXPIRES_AT))
            .where(RefreshTokens.REFRESH_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteByToken(RefreshToken token) {
    final int rows =
        db.delete(RefreshTokens.REFRESH_TOKENS)
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
            .execute();
    return rows > 0;
  }
}
