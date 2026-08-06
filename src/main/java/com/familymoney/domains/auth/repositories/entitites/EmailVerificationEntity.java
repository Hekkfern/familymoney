package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;

public record EmailVerificationEntity(
    UserId userId,
    Instant createdAt,
    Instant updatedAt,
    ExpirationTime expiresAt,
    Instant lastSentAt) {}
