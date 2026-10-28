package com.familymoney.domains.auth.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.auth.repositories.dbos.CreateEmailVerificationDbo;
import com.familymoney.domains.auth.repositories.dbos.UpdateEmailVerificationTokenDbo;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.mappers.EmailVerificationJooqMapper;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.EmailVerificationTokens;
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
public class DefaultEmailVerificationRepository implements EmailVerificationRepository {

  private final DSLContext db;
  private final OpaqueTokenHasher tokenHasher;

  @Override
  public void create(final CreateEmailVerificationDbo data) {
    db.insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .columns(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT)
        .values(
            data.userId().value(),
            tokenHasher.hash(data.token().value()),
            data.expiresAt().toOffsetDateTime())
        .execute();
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
  public void updateByUserId(final UserId userId, final UpdateEmailVerificationTokenDbo data) {
    final Map<Field<?>, Object> changedFields = new HashMap<>();
    if (data.token() != null) {
      changedFields.put(
          EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH,
          tokenHasher.hash(data.token().value()));
    }
    if (data.expiresAt() != null) {
      changedFields.put(
          EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
          data.expiresAt().toOffsetDateTime());
    }
    if (data.lastSentAt() != null) {
      changedFields.put(
          EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT,
          data.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET));
    }
    db.update(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .set(changedFields)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
        .execute();
  }

  @Override
  public void deleteByUserId(final UserId userId) {
    db.deleteFrom(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
        .execute();
  }
}
