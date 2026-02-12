package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.familymoney.repositories.entities.EmailVerificationEntity;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class EmailVerificationJooqMapper {

  private EmailVerificationJooqMapper() {}

  public static EmailVerificationEntity toEntity(final Record r) {
    OffsetDateTime createdAt = r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT);
    OffsetDateTime expiresAt = r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT);

    return EmailVerificationEntity.builder()
        .id(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID))
        .userId(UserId.fromUuid(r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID)))
        .token(
            EmailVerificationToken.fromString(
                r.get(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN)))
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .expiresAt(expiresAt != null ? expiresAt.toInstant() : null)
        .build();
  }
}
