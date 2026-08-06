package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.EmailVerificationTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class EmailVerificationJooqMapper {

  private EmailVerificationJooqMapper() {
    /* this class is not meant to be instantiated */
  }

  public static EmailVerificationEntity toEntity(final Record r) {
    final OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT));
    final OffsetDateTime updatedAt =
        Objects.requireNonNull(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT));
    final OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT));
    final OffsetDateTime lastSentAt =
        Objects.requireNonNull(
            r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.LAST_SENT_AT));

    return new EmailVerificationEntity(
        UserId.fromUuid(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID)),
        createdAt.toInstant(),
        updatedAt.toInstant(),
        ExpirationTime.of(expiresAt),
        lastSentAt.toInstant());
  }
}
