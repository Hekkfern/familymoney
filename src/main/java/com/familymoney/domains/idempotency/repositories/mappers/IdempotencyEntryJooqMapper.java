package com.familymoney.domains.idempotency.repositories.mappers;

import com.familymoney.domains.idempotency.repositories.dtos.CachedResponseDto;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyState;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.IdempotencyKeys;
import java.util.Objects;
import org.jooq.JSONB;
import org.jooq.Record;

public final class IdempotencyEntryJooqMapper {

  private IdempotencyEntryJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static IdempotencyEntry toEntity(final Record record) {
    final Integer responseStatus = record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_STATUS);
    final JSONB responseBody = record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_BODY);
    final boolean isCompleted = responseStatus != null && responseBody != null;
    final CachedResponseDto response =
        isCompleted ? new CachedResponseDto(responseStatus, responseBody.data()) : null;

    return new IdempotencyEntry(
        UserId.fromUuid(
            Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID))),
        IdempotencyKey.fromUuid(
            Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.KEY))),
        Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.REQUEST_HASH)),
        isCompleted ? IdempotencyState.COMPLETED : IdempotencyState.IN_PROGRESS,
        response,
        Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT))
            .toInstant());
  }
}
