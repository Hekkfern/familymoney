package com.familymoney.domains.users.types;

import com.familymoney.utils.UUIDGenerator;
import java.util.UUID;

public record UserId(UUID value) {

  public static UserId fromString(final String value) {
    return new UserId(UUID.fromString(value));
  }

  public static UserId fromUuid(final UUID value) {
    return new UserId(value);
  }

  public static UserId generate() {
    return new UserId(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
