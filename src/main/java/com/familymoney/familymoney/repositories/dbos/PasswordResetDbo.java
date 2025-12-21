package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PasswordResetDbo(
    UUID id, UserId userId, PasswordResetToken token, Instant createdAt, Instant expiresAt) {}
