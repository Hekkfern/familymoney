package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;

/**
 * DTO for creating an email verification.
 *
 * @param userId ID of the user
 * @param token Email Verification token
 * @param expiresAt Timestamp when the token expires
 */
@Builder
public record CreateEmailVerificationDto(
    UserId userId, EmailVerificationToken token, Instant expiresAt) {}
