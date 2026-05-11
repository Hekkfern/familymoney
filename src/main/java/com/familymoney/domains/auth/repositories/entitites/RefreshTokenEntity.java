package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record RefreshTokenEntity(
    UUID id,
    UserId userId,
    RefreshToken token,
    Instant createdAt,
    Instant expiresAt,
    TokenFamily family) {}
