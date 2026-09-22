package com.familymoney.domains.idempotency.repositories.dtos;

import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;

public record ReserveIdempotencyKeyDto(UserId userId, IdempotencyKey key, byte[] requestHash) {}
