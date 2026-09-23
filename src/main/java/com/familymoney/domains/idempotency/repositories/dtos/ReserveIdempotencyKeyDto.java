package com.familymoney.domains.idempotency.repositories.dtos;

import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import java.util.Arrays;
import java.util.Objects;

public record ReserveIdempotencyKeyDto(UserId userId, IdempotencyKey key, byte[] requestHash) {

  public ReserveIdempotencyKeyDto {
    requestHash = Arrays.copyOf(requestHash, requestHash.length);
  }

  @Override
  public byte[] requestHash() {
    return Arrays.copyOf(requestHash, requestHash.length);
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof ReserveIdempotencyKeyDto other)) {
      return false;
    }

    return Objects.equals(userId, other.userId)
        && Objects.equals(key, other.key)
        && Arrays.equals(requestHash, other.requestHash);
  }

  @Override
  public int hashCode() {
    final int fieldsHash = Objects.hash(userId, key);
    return 31 * fieldsHash + Arrays.hashCode(requestHash);
  }

  @Override
  public String toString() {
    return "ReserveIdempotencyKeyDto[userId=%s, key=%s, requestHash=%s]"
        .formatted(userId, key, Arrays.toString(requestHash));
  }
}
