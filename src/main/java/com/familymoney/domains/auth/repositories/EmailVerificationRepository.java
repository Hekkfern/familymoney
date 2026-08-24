package com.familymoney.domains.auth.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.mappers.EmailVerificationJooqMapper;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.security.IOpaqueTokenHasher;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository implements IEmailVerificationRepository {

  private final DSLContext db;
  private final IOpaqueTokenHasher tokenHasher;

  @Override
  public Optional<EmailVerificationEntity> create(final CreateEmailVerificationDto data) {
    return db.insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .columns(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT)
        .values(
            data.userId().value(),
            tokenHasher.hash(data.token().value()),
            data.expiresAt().toOffsetDateTime())
        .returning(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT)
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
  }

  @Override
  public Optional<EmailVerificationEntity> findByUserId(final UserId userId) {
    return db.select(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT)
        .from(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
  }

  @Override
  public Optional<EmailVerificationEntity> findByToken(final EmailVerificationToken token) {
    return db.select(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT)
        .from(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH.eq(
                tokenHasher.hash(token.value())))
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
  }

  @Override
  public boolean updateByUserId(final UserId userId, final UpdateEmailVerificationTokenDto data) {
    final OffsetDateTime lastSentAt =
        data.lastSentAt() != null ? data.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET) : null;
    final int rowsAffected =
        db.update(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
            .set(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH,
                DSL.coalesce(
                    DSL.val(data.token() != null ? tokenHasher.hash(data.token().value()) : null),
                    EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH))
            .set(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
                DSL.coalesce(
                    DSL.val(data.expiresAt() != null ? data.expiresAt().toOffsetDateTime() : null),
                    EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT))
            .set(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT,
                DSL.coalesce(
                    DSL.val(lastSentAt),
                    EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT))
            .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    final int rowsAffected =
        db.deleteFrom(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
            .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }
}
