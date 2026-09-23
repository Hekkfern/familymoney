package com.familymoney.domains.idempotency.services;

import com.familymoney.domains.idempotency.repositories.IdempotencyRepository;
import com.familymoney.domains.idempotency.repositories.dtos.CachedResponseDto;
import com.familymoney.domains.idempotency.repositories.dtos.ReserveIdempotencyKeyDto;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultIdempotencyService implements IdempotencyService {

  private final IdempotencyRepository idempotencyRepository;

  @Override
  public boolean reserve(UserId userId, IdempotencyKey key, byte[] requestHash) {
    return idempotencyRepository.reserve(new ReserveIdempotencyKeyDto(userId, key, requestHash));
  }

  @Override
  public void complete(
      UserId userId, IdempotencyKey key, int responseHttpStatus, String responseBody) {
    idempotencyRepository.complete(
        userId, key, new CachedResponseDto(responseHttpStatus, responseBody));
  }

  @Override
  public Optional<IdempotencyEntry> findByKey(UserId userId, IdempotencyKey key) {
    return idempotencyRepository.findByKey(userId, key);
  }
}
