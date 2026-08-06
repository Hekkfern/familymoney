package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.UUID;

public record RefreshTokenEntity(
    UUID id,
    UserId userId,
    Instant createdAt,
    Instant updatedAt,
    ExpirationTime expiresAt,
    TokenFamily family) {}
