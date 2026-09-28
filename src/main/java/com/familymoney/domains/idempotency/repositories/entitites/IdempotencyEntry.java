package com.familymoney.domains.idempotency.repositories.entitites;

import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record IdempotencyEntry(
    UserId userId,
    IdempotencyKey key,
    byte[] requestHash,
    IdempotencyState state,
    @Nullable String responseBody,
    Instant expiresAt) {

  public IdempotencyEntry {
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
    if (!(object instanceof IdempotencyEntry other)) {
      return false;
    }

    return Objects.equals(userId, other.userId)
        && Objects.equals(key, other.key)
        && Arrays.equals(requestHash, other.requestHash)
        && state == other.state
        && Objects.equals(responseBody, other.responseBody)
        && Objects.equals(expiresAt, other.expiresAt);
  }

  @Override
  public int hashCode() {
    final int fieldsHash = Objects.hash(userId, key, state, responseBody, expiresAt);
    return 31 * fieldsHash + Arrays.hashCode(requestHash);
  }

  @Override
  public String toString() {
    return "IdempotencyEntry[userId=%s, key=%s, requestHash=%s, state=%s, responseBody=%s, expiresAt=%s]"
        .formatted(userId, key, Arrays.toString(requestHash), state, responseBody, expiresAt);
  }
}
