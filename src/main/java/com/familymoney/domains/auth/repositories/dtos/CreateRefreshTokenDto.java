package com.familymoney.domains.auth.repositories.dtos;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.UserId;
import java.util.UUID;

/**
 * DTO for creating a new refresh token record in the database *
 *
 * @param id the unique identifier for the refresh token record.
 * @param userId the ID of the user associated with the refresh token.
 * @param token the actual refresh token string that will be stored in the database.
 * @param family a UUID representing the token family. Tokens that belong to the same family can be
 *     managed/rotated/revoked together.
 * @param expiresAt the timestamp indicating when the refresh token will expire and should no longer
 *     be accepted for authentication.
 */
public record CreateRefreshTokenDto(
    UUID id, UserId userId, RefreshToken token, TokenFamily family, ExpirationTime expiresAt) {}
