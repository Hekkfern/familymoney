package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;

@Builder
public record EmailVerificationEntity(
    UserId userId,
    EmailVerificationToken token,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt) {}
