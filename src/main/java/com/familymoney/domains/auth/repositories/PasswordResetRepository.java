package com.familymoney.domains.auth.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.auth.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.domains.auth.repositories.dtos.UpdatePasswordResetDto;
import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.repositories.mappers.PasswordResetJooqMapper;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.PasswordResetTokens;
import com.familymoney.security.IOpaqueTokenHasher;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PasswordResetRepository implements IPasswordResetRepository {

  private final DSLContext db;
  private final IOpaqueTokenHasher tokenHasher;

  @Override
  public Optional<PasswordResetEntity> create(final CreatePasswordResetDto data) {
    return db.insertInto(PasswordResetTokens.PASSWORD_RESET_TOKENS)
        .columns(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT)
        .values(
            data.userId().value(),
            tokenHasher.hash(data.token().value()),
            data.expiresAt().toOffsetDateTime(),
            data.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET))
        .returning(
            PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.UPDATED_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
            PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT)
        .fetchOptional()
        .map(PasswordResetJooqMapper::toEntity);
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
  public boolean updateByUserId(final UserId userId, final UpdatePasswordResetDto data) {
    final OffsetDateTime expiresAtVal =
        data.expiresAt() != null ? data.expiresAt().toOffsetDateTime() : null;
    final int rowsAffected =
        db.update(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .set(
                PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH,
                DSL.coalesce(
                    DSL.val(data.token() != null ? tokenHasher.hash(data.token().value()) : null),
                    PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH))
            .set(
                PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT,
                DSL.coalesce(
                    DSL.val(expiresAtVal), PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT))
            .set(
                PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT,
                DSL.coalesce(
                    DSL.val(
                        data.lastSentAt() != null
                            ? data.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET)
                            : null),
                    PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT))
            .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    final int rowsAffected =
        db.deleteFrom(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }
}
