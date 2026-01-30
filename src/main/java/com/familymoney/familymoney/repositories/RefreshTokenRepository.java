package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.generated.tables.RefreshTokens;
import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateRefreshTokenDbo;
import com.familymoney.familymoney.repositories.mappers.RefreshTokenJooqMapper;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
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
  public Optional<RefreshTokenDbo> create(
      final UserId userId, final RefreshToken token, final UUID family) {
    return db.insertInto(RefreshTokens.REFRESH_TOKENS)
        .columns(
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .values(userId.value(), token.value(), family)
        .returning(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.IS_USED,
            RefreshTokens.REFRESH_TOKENS.USED_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .fetchOptional()
        .map(RefreshTokenJooqMapper::toDbo);
  }

  @Override
  public Optional<RefreshTokenDbo> findByToken(final RefreshToken token) {
    return db.select(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.IS_USED,
            RefreshTokens.REFRESH_TOKENS.USED_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .from(RefreshTokens.REFRESH_TOKENS)
        .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(RefreshTokenJooqMapper::toDbo);
  }

  @Override
  public boolean updateByToken(final RefreshToken token, final UpdateRefreshTokenDbo data) {
    val usedAtVal =
        data.getUsedAt() != null
            ? OffsetDateTime.ofInstant(data.getUsedAt(), ZoneOffset.UTC)
            : null;
    val rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.IS_USED,
                DSL.coalesce(DSL.val(data.getIsUsed()), RefreshTokens.REFRESH_TOKENS.IS_USED))
            .set(
                RefreshTokens.REFRESH_TOKENS.USED_AT,
                DSL.coalesce(DSL.val(usedAtVal), RefreshTokens.REFRESH_TOKENS.USED_AT))
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(token.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean updateByFamily(final UUID family, final UpdateRefreshTokenDbo data) {
    val usedAtVal =
        data.getUsedAt() != null
            ? OffsetDateTime.ofInstant(data.getUsedAt(), ZoneOffset.UTC)
            : null;
    val rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.IS_USED,
                DSL.coalesce(DSL.val(data.getIsUsed()), RefreshTokens.REFRESH_TOKENS.IS_USED))
            .set(
                RefreshTokens.REFRESH_TOKENS.USED_AT,
                DSL.coalesce(DSL.val(usedAtVal), RefreshTokens.REFRESH_TOKENS.USED_AT))
            .where(RefreshTokens.REFRESH_TOKENS.FAMILY.eq(family))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean updateByUserId(final UserId userId, final UpdateRefreshTokenDbo data) {
    val usedAtVal =
        data.getUsedAt() != null
            ? OffsetDateTime.ofInstant(data.getUsedAt(), ZoneOffset.UTC)
            : null;
    val rowsAffected =
        db.update(RefreshTokens.REFRESH_TOKENS)
            .set(
                RefreshTokens.REFRESH_TOKENS.IS_USED,
                DSL.coalesce(DSL.val(data.getIsUsed()), RefreshTokens.REFRESH_TOKENS.IS_USED))
            .set(
                RefreshTokens.REFRESH_TOKENS.USED_AT,
                DSL.coalesce(DSL.val(usedAtVal), RefreshTokens.REFRESH_TOKENS.USED_AT))
            .where(RefreshTokens.REFRESH_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public void deleteOlderThan(final Duration cutoff) {
    val threshold = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(cutoff.getSeconds());
    db.deleteFrom(RefreshTokens.REFRESH_TOKENS)
        .where(RefreshTokens.REFRESH_TOKENS.CREATED_AT.lt(threshold))
        .execute();
  }
}
