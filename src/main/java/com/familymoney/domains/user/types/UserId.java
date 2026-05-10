package com.familymoney.domains.user.types;

import com.familymoney.utils.UUIDGenerator;
import java.util.UUID;

public record UserId(UUID value) {

  public static UserId fromString(String value) {
    return new UserId(UUID.fromString(value));
  }

  public static UserId fromUuid(UUID value) {
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
