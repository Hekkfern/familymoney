package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.PasswordResetTokens;
import com.familymoney.familymoney.repositories.entities.PasswordResetEntity;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class PasswordResetJooqMapper {

  private PasswordResetJooqMapper() {}

  public static PasswordResetEntity toEntity(final Record r) {
    OffsetDateTime createdAt = r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT);
    OffsetDateTime expiresAt = r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT);

    return PasswordResetEntity.builder()
        .id(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.ID))
        .userId(UserId.fromUuid(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID)))
        .token(
            PasswordResetToken.fromString(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN)))
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .expiresAt(expiresAt != null ? expiresAt.toInstant() : null)
        .build();
  }
}
