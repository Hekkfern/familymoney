package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.PasswordResetTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class PasswordResetJooqMapper {

  private PasswordResetJooqMapper() {}

  public static PasswordResetEntity toEntity(final Record r) {
    OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT));
    OffsetDateTime updatedAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.UPDATED_AT));
    OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT));

    return PasswordResetEntity.builder()
        .userId(UserId.fromUuid(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID)))
        .token(
            PasswordResetToken.fromString(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN)))
        .createdAt(createdAt.toInstant())
        .updatedAt(updatedAt.toInstant())
        .expiresAt(expiresAt.toInstant())
        .build();
  }
}
