package com.familymoney.familymoney.repositories.impl;

import com.familymoney.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import com.familymoney.familymoney.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.familymoney.repositories.entities.EmailVerificationEntity;
import com.familymoney.familymoney.repositories.mappers.EmailVerificationJooqMapper;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
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
  public Optional<EmailVerificationEntity> create(final CreateEmailVerificationDto data) {
    return db.insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .columns(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT)
        .values(
            data.id(),
            data.userId().value(),
            data.token().value(),
            OffsetDateTime.ofInstant(data.expiresAt(), ZoneOffset.UTC))
        .returning(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT)
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
  }

  @Override
  public Optional<EmailVerificationEntity> findByToken(final EmailVerificationToken token) {
    return db.select(
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
            EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT)
        .from(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
        .where(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(EmailVerificationJooqMapper::toEntity);
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
