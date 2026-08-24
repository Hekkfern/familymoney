package com.familymoney.domains.auth.repositories.mappers;

import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.PasswordResetTokens;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class PasswordResetJooqMapper {

  private PasswordResetJooqMapper() {
    /* this class is not meant to be instantiated */
  }

  /**
   * Maps a password reset database record to its domain entity.
   *
   * @param r database record containing password reset fields
   * @return mapped password reset entity
   * @throws NullPointerException when a required database field is null
   */
  public static PasswordResetEntity toEntity(final Record r) {
    final OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.CREATED_AT));
    final OffsetDateTime updatedAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.UPDATED_AT));
    final OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.EXPIRES_AT));
    final OffsetDateTime lastSentAt =
        Objects.requireNonNull(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.LAST_SENT_AT));

    return new PasswordResetEntity(
        UserId.fromUuid(r.get(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID)),
        createdAt.toInstant(),
        updatedAt.toInstant(),
        ExpirationTime.of(expiresAt),
        lastSentAt.toInstant());
  }
}
