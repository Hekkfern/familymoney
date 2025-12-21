package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record EmailVerificationDbo(
    UUID id, UserId userId, EmailVerificationToken token, Instant createdAt, Instant expiresAt) {}
