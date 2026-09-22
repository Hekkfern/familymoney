package com.familymoney.domains.idempotency.repositories;

import com.familymoney.domains.idempotency.repositories.dtos.CachedResponseDto;
import com.familymoney.domains.idempotency.repositories.dtos.ReserveIdempotencyKeyDto;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;

public interface IdempotencyRepository {

  boolean reserve(ReserveIdempotencyKeyDto dto);

  void complete(UserId userId, IdempotencyKey key, CachedResponseDto dto);

  Optional<IdempotencyEntry> findByKey(UserId userId, IdempotencyKey key);

  int deleteExpired(int batchSize);
}
