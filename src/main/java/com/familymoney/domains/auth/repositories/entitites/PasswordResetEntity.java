package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;

public record PasswordResetEntity(
    UserId userId,
    PasswordResetToken token,
    Instant createdAt,
    Instant updatedAt,
    ExpirationTime expiresAt) {}
