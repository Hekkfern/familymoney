package com.familymoney.familymoney.types;

import java.util.UUID;

public record UserId(UUID value) {

  public static UserId fromString(String value) {
    return new UserId(UUID.fromString(value));
  }

  public static UserId fromUuid(UUID value) {
    return new UserId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
