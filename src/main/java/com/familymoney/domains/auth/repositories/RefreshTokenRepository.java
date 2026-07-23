package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.repositories.mappers.RefreshTokenJooqMapper;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.RefreshTokens;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository implements IRefreshTokenRepository {

  private final DSLContext db;

  @Override
  public Optional<RefreshTokenEntity> create(final CreateRefreshTokenDto data) {
    return db.insertInto(RefreshTokens.REFRESH_TOKENS)
        .columns(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.FAMILY,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT)
        .values(
            data.id(),
            data.userId().value(),
            data.token().value(),
            data.family().value(),
            data.expiresAt().toOffsetDateTime())
        .returning(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
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
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.UPDATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .from(RefreshTokens.REFRESH_TOKENS)
        .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(RefreshTokenJooqMapper::toEntity);
  }

  @Override
  public boolean updateByToken(final RefreshToken token, final UpdateRefreshTokenDto data) {
    val expiresAtVal = data.expiresAt() != null ? data.expiresAt().toOffsetDateTime() : null;
    val rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.TOKEN,
                DSL.coalesce(
                    DSL.val(data.token() != null ? data.token().value() : null),
                    RefreshTokens.REFRESH_TOKENS.TOKEN))
            .set(
                RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
                DSL.coalesce(DSL.val(expiresAtVal), RefreshTokens.REFRESH_TOKENS.EXPIRES_AT))
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(token.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean updateByUserId(final UserId userId, final UpdateRefreshTokenDto data) {
    val expiresAtVal = data.expiresAt() != null ? data.expiresAt().toOffsetDateTime() : null;
    val rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.TOKEN,
                DSL.coalesce(
                    DSL.val(data.token() != null ? data.token().value() : null),
                    RefreshTokens.REFRESH_TOKENS.TOKEN))
            .set(
                RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
                DSL.coalesce(DSL.val(expiresAtVal), RefreshTokens.REFRESH_TOKENS.EXPIRES_AT))
            .where(RefreshTokens.REFRESH_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteByToken(RefreshToken token) {
    val rows =
        db.delete(RefreshTokens.REFRESH_TOKENS)
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(token.value()))
            .execute();
    return rows > 0;
  }
}
