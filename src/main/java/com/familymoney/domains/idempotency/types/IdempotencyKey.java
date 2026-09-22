package com.familymoney.domains.idempotency.types;

import com.familymoney.utils.UUIDGenerator;
import java.util.UUID;

public record IdempotencyKey(UUID value) {

  public static IdempotencyKey fromString(final String value) {
    return new IdempotencyKey(UUID.fromString(value));
  }

  public static IdempotencyKey fromUuid(final UUID value) {
    return new IdempotencyKey(value);
  }

  public static IdempotencyKey generate() {
    return new IdempotencyKey(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
