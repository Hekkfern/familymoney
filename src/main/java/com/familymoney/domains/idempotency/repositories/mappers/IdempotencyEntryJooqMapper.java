package com.familymoney.domains.idempotency.repositories.mappers;

import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyEntry;
import com.familymoney.domains.idempotency.repositories.entitites.IdempotencyState;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.IdempotencyKeys;
import java.util.Objects;
import org.jooq.JSONB;
import org.jooq.Record;
import org.jspecify.annotations.Nullable;

public final class IdempotencyEntryJooqMapper {

  private IdempotencyEntryJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static IdempotencyEntry toEntity(final Record record) {
    final com.familymoney.generated.enums.IdempotencyState state =
        Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.STATE));
    final JSONB responseBody = record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.RESPONSE_BODY);
    final @Nullable String body = responseBody != null ? responseBody.data() : null;

    return new IdempotencyEntry(
        UserId.fromUuid(
            Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.USER_ID))),
        IdempotencyKey.fromUuid(
            Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.KEY))),
        Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.REQUEST_HASH)),
        IdempotencyState.valueOf(state.name()),
        body,
        Objects.requireNonNull(record.get(IdempotencyKeys.IDEMPOTENCY_KEYS.EXPIRES_AT))
            .toInstant());
  }
}
