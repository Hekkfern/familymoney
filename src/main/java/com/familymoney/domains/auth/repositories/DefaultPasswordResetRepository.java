package com.familymoney.domains.auth.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.auth.repositories.dbos.CreatePasswordResetDbo;
import com.familymoney.domains.auth.repositories.dbos.UpdatePasswordResetDbo;
import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.repositories.mappers.PasswordResetJooqMapper;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.PasswordResetTokens;
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
public class DefaultPasswordResetRepository implements PasswordResetRepository {

  private final DSLContext db;
  private final OpaqueTokenHasher tokenHasher;

  @Override
  public void create(final CreatePasswordResetDbo dbo) {
    db.insertInto(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .columns(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT)
        .values(
            dbo.userId().value(),
            tokenHasher.hash(dbo.token().value()),
            dbo.expiresAt().toOffsetDateTime(),
            dbo.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET))
        .execute();
  }

  @Override
  public Optional<PasswordResetEntity> findByUserId(final UserId userId) {
    return db.select(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.UPDATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT)
        .from(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID.eq(userId.value()))
        .fetchOptional()
        .map(PasswordResetJooqMapper::toEntity);
  }

  @Override
  public Optional<PasswordResetEntity> findByToken(final PasswordResetToken token) {
    return db.select(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.UPDATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT)
        .from(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .where(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH.eq(
                tokenHasher.hash(token.value())))
        .fetchOptional()
        .map(PasswordResetJooqMapper::toEntity);
  }

  @Override
  public void updateByUserId(final UserId userId, final UpdatePasswordResetDbo data) {
    final Map<Field<?>, Object> changedFields = new HashMap<>();
    if (data.token() != null) {
      changedFields.put(
          PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH,
          tokenHasher.hash(data.token().value()));
    }
    if (data.expiresAt() != null) {
      changedFields.put(
          PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
          data.expiresAt().toOffsetDateTime());
    }
    if (data.lastSentAt() != null) {
      changedFields.put(
          PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT,
          data.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET));
    }
    db.update(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .set(changedFields)
        .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID.eq(userId.value()))
        .execute();
  }

  @Override
  public void deleteByUserId(final UserId userId) {
    db.deleteFrom(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID.eq(userId.value()))
        .execute();
  }
}
