package com.familymoney.domains.idempotency.repositories.entitites;

import com.familymoney.domains.idempotency.repositories.dtos.CachedResponseDto;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record IdempotencyEntry(
    UserId userId,
    IdempotencyKey key,
    byte[] requestHash,
    IdempotencyState state,
    @Nullable CachedResponseDto response,
    Instant expiresAt) {}
