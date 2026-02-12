package com.familymoney.familymoney.repositories.dtos;

import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for creating a new refresh token record in the database *
 *
 * @param id the unique identifier for the refresh token record.
 * @param userId the ID of the user associated with the refresh token.
 * @param token the actual refresh token string that will be stored in the database.
 * @param family a UUID representing the token family. Tokens that belong to the same family can be
 *     managed/rotated/revoked together.
 */
@Builder
public record CreateRefreshTokenDto(UUID id, UserId userId, RefreshToken token, UUID family) {}
