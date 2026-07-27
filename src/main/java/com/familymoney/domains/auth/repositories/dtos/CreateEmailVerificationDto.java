package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.users.types.UserId;

/**
 * DTO for creating an email verification.
 *
 * @param userId ID of the user
 * @param token Email Verification token
 * @param expiresAt Timestamp when the token expires
 */
public record CreateEmailVerificationDto(
    UserId userId, EmailVerificationToken token, ExpirationTime expiresAt) {}
