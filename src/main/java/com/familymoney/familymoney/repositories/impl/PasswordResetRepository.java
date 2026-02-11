package com.familymoney.familymoney.repositories.impl;

import com.familymoney.familymoney.generated.tables.PasswordResetTokens;
import com.familymoney.familymoney.repositories.IPasswordResetRepository;
import com.familymoney.familymoney.repositories.dbos.PasswordResetDbo;
import com.familymoney.familymoney.repositories.mappers.PasswordResetJooqMapper;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PasswordResetRepository implements IPasswordResetRepository {

  private final DSLContext db;

  @Override
  public Optional<PasswordResetDbo> create(
      final UserId userId, final PasswordResetToken token, final Instant expiresAt) {
    return db.insertInto(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .columns(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT)
        .values(userId.value(), token.value(), OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
        .returning(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT)
        .fetchOptional()
        .map(PasswordResetJooqMapper::toDbo);
  }

  @Override
  public Optional<PasswordResetDbo> findByToken(final PasswordResetToken token) {
    return db.select(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT)
        .from(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(PasswordResetJooqMapper::toDbo);
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    val rowsAffected =
        db.deleteFrom(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }
}
