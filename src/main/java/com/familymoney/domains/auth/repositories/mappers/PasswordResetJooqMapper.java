package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.PasswordResetTokens;
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
