package com.familymoney.repositories.dtos;

import com.familymoney.types.EmailVerificationToken;
import com.familymoney.types.UserId;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for creating an email verification.
 *
 * @param id the unique identifier for the email verification record.
 * @param userId ID of the user
 * @param token Email Verification token
 * @param expiresAt Timestamp when the token expires
 */
@Builder
public record CreateEmailVerificationDto(
    UUID id, UserId userId, EmailVerificationToken token, Instant expiresAt) {}
