package com.familymoney.repositories.entities;

import com.familymoney.types.PasswordResetToken;
import com.familymoney.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PasswordResetEntity(
    UUID id, UserId userId, PasswordResetToken token, Instant createdAt, Instant expiresAt) {}
