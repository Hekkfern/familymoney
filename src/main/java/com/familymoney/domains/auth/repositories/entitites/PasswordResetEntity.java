package com.familymoney.domains.auth.repositories.entitites;

import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PasswordResetEntity(
    UUID id, UserId userId, PasswordResetToken token, Instant createdAt, Instant expiresAt) {}
