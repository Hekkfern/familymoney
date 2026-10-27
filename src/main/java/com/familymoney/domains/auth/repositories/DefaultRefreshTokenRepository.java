package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dbos.CreateRefreshTokenDbo;
import com.familymoney.domains.auth.repositories.dbos.UpdateRefreshTokenDbo;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.repositories.mappers.RefreshTokenJooqMapper;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.RefreshTokens;
import com.familymoney.security.OpaqueTokenHasher;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DefaultRefreshTokenRepository implements RefreshTokenRepository {

  private final DSLContext db;
  private final OpaqueTokenHasher tokenHasher;

  @Override
  public void create(final CreateRefreshTokenDbo data) {
    db.insertInto(RefreshTokens.REFRESH_TOKENS)
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
        .execute();
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
  public void updateByToken(final RefreshToken token, final UpdateRefreshTokenDbo data) {
    final Map<Field<?>, Object> changedFields = changedFields(data);
    db.update(RefreshTokens.REFRESH_TOKENS)
        .set(changedFields)
        .where(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
        .execute();
  }

  @Override
  public void updateByUserId(final UserId userId, final UpdateRefreshTokenDbo data) {
    final Map<Field<?>, Object> changedFields = changedFields(data);
    db.update(RefreshTokens.REFRESH_TOKENS)
        .set(changedFields)
        .where(RefreshTokens.REFRESH_TOKENS.USER_ID.eq(userId.value()))
        .execute();
  }

  private Map<Field<?>, Object> changedFields(final UpdateRefreshTokenDbo data) {
    final Map<Field<?>, Object> changedFields = new HashMap<>();
    if (data.token() != null) {
      changedFields.put(
          RefreshTokens.REFRESH_TOKENS.TOKEN_HASH, tokenHasher.hash(data.token().value()));
    }
    if (data.expiresAt() != null) {
      changedFields.put(
          RefreshTokens.REFRESH_TOKENS.EXPIRES_AT, data.expiresAt().toOffsetDateTime());
    }
    if (changedFields.isEmpty()) {
      changedFields.put(
          RefreshTokens.REFRESH_TOKENS.TOKEN_HASH, RefreshTokens.REFRESH_TOKENS.TOKEN_HASH);
    }
    return changedFields;
  }

  @Override
  public void deleteByToken(RefreshToken token) {
    db.delete(RefreshTokens.REFRESH_TOKENS)
        .where(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
        .execute();
  }

  @Override
  public void deleteByUserId(final UserId userId) {
    db.deleteFrom(RefreshTokens.REFRESH_TOKENS)
        .where(RefreshTokens.REFRESH_TOKENS.USER_ID.eq(userId.value()))
        .execute();
  }
}
