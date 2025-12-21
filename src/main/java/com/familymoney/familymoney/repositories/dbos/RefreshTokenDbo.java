package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RefreshTokenDbo(
    UUID id,
    UserId userId,
    RefreshToken token,
    Instant createdAt,
    Instant expiresAt,
    boolean isUsed,
    Optional<Instant> usedAt,
    UUID family) {}
