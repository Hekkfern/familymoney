package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;

/**
 * DTO for creating a new password reset record in the database
 *
 * @param userId ID of the user for whom the password reset record is being created
 * @param token the unique token associated with the password reset request, used for validation and
 *     retrieval
 * @param expiresAt the timestamp indicating when the password reset token expires, after which it
 *     should no longer be valid
 */
@Builder
public record CreatePasswordResetDto(UserId userId, PasswordResetToken token, Instant expiresAt) {}
