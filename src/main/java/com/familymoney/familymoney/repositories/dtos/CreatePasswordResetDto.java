package com.familymoney.familymoney.repositories.dtos;

import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

/**
 * DTO for creating a new password reset record in the database
 *
 * @param id the unique identifier for the password reset record
 * @param userId ID of the user for whom the password reset record is being created
 * @param token the unique token associated with the password reset request, used for validation and
 *     retrieval
 * @param expiresAt the timestamp indicating when the password reset token expires, after which it
 *     should no longer be valid
 */
@Builder
public record CreatePasswordResetDto(
    UUID id, UserId userId, PasswordResetToken token, Instant expiresAt) {}
