package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.PasswordResetTokens;
import com.familymoney.familymoney.repositories.dbos.PasswordResetDbo;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class PasswordResetJooqMapper {

  private PasswordResetJooqMapper() {}

  public static PasswordResetDbo toDbo(final Record r) {
    OffsetDateTime createdAt = r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT);
    OffsetDateTime expiresAt = r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT);

    return PasswordResetDbo.builder()
        .id(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.ID))
        .userId(UserId.fromUuid(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID)))
        .token(
            PasswordResetToken.fromString(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN)))
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .expiresAt(expiresAt != null ? expiresAt.toInstant() : null)
        .build();
  }
}
