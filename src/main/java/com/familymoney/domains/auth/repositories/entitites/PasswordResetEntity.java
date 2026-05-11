package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PasswordResetEntity(
    UserId userId,
    PasswordResetToken token,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt) {}
