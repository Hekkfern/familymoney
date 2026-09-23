package com.familymoney.domains.idempotency.services;

import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

public interface IdempotencyService {

  boolean reserve(UserId userId, IdempotencyKey key, byte[] requestHash);

  void complete(UserId userId, IdempotencyKey key, int responseHttpStatus, String responseBody);

  Optional<IdempotencyEntry> findByKey(UserId userId, IdempotencyKey key);
}
