package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.UserId;

/**
 * DTO for creating a new password reset record in the database
 *
 * @param userId ID of the user for whom the password reset record is being created
 * @param token the unique token associated with the password reset request, used for validation and
 *     retrieval
 * @param expiresAt the timestamp indicating when the password reset token expires, after which it
 *     should no longer be valid
 */
public record CreatePasswordResetDto(
    UserId userId, PasswordResetToken token, ExpirationTime expiresAt) {}
