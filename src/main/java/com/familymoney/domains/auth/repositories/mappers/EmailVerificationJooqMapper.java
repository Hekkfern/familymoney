package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.EmailVerificationTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class EmailVerificationJooqMapper {

  private EmailVerificationJooqMapper() {}

  public static EmailVerificationEntity toEntity(final Record r) {
    OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT));
    OffsetDateTime updatedAt =
        Objects.requireNonNull(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.UPDATED_AT));
    OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT));

    return EmailVerificationEntity.builder()
        .userId(UserId.fromUuid(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID)))
        .token(
            EmailVerificationToken.fromString(
                r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN)))
        .createdAt(createdAt.toInstant())
        .updatedAt(updatedAt.toInstant())
        .expiresAt(expiresAt.toInstant())
        .build();
  }
}
