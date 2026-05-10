package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.user.types.UserId;
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
