package com.familymoney.familymoney.repositories.entities;

import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RefreshTokenEntity(
    UUID id,
    UserId userId,
    RefreshToken token,
    Instant createdAt,
    Instant expiresAt,
    boolean isUsed,
    Optional<Instant> usedAt,
    UUID family) {}
