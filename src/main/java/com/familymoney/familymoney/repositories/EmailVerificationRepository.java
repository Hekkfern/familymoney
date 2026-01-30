package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.repositories.mappers.EmailVerificationJooqMapper;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Duration;
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
public class EmailVerificationRepository implements IEmailVerificationRepository {

  private final DSLContext db;

  @Override
  public Optional<EmailVerificationDbo> create(
      final UserId userId, final EmailVerificationToken token, final Instant expiresAt) {
    return db.insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .columns(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT)
        .values(userId.value(), token.value(), OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
        .returning(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT)
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toDbo);
  }

  @Override
  public Optional<EmailVerificationDbo> findByToken(final EmailVerificationToken token) {
    return db.select(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT)
        .from(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toDbo);
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    val rowsAffected =
        db.deleteFrom(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
            .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public void deleteOlderThan(final Duration cutoff) {
    val threshold = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(cutoff.getSeconds());
    db.deleteFrom(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT.lt(threshold))
        .execute();
  }
}
