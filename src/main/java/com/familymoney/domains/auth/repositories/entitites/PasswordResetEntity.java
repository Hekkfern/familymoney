package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;

/**
 * Password reset token metadata stored for a user.
 *
 * @param userId identifier of the token owner
 * @param createdAt token creation timestamp
 * @param updatedAt token update timestamp
 * @param expiresAt token expiration timestamp
 * @param lastSentAt timestamp when the reset email was last sent
 */
public record PasswordResetEntity(
    UserId userId,
    Instant createdAt,
    Instant updatedAt,
    ExpirationTime expiresAt,
    Instant lastSentAt) {}
