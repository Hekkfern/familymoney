package com.familymoney.domains.auth.repositories;

import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.repositories.mappers.EmailVerificationJooqMapper;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.EmailVerificationTokens;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepository implements IEmailVerificationRepository {

  private final DSLContext db;

  @Override
  public Optional<EmailVerificationEntity> create(final CreateEmailVerificationDto data) {
    return db.insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .columns(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT)
        .values(
            data.userId().value(),
            data.token().value(),
            OffsetDateTime.ofInstant(data.expiresAt(), ZoneOffset.UTC))
        .returning(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT)
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
  }

  @Override
  public Optional<EmailVerificationEntity> findByUserId(final UserId userId) {
    return db.select(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
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
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT)
        .from(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
  }

  @Override
  public boolean updateByUserId(final UserId userId, final UpdateEmailVerificationTokenDto data) {
    val rowsAffected =
        db.update(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
            .set(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
                DSL.coalesce(
                    DSL.val(data.getToken() != null ? data.getToken().value() : null),
                    EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN))
            .set(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
                DSL.coalesce(
                    DSL.val(
                        data.getExpiresAt() != null
                            ? OffsetDateTime.ofInstant(data.getExpiresAt(), ZoneOffset.UTC)
                            : null),
                    EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT))
            .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteByUserId(final UserId userId) {
    val rowsAffected =
        db.deleteFrom(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
            .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID.eq(userId.value()))
            .execute();
    return rowsAffected > 0;
  }
}
