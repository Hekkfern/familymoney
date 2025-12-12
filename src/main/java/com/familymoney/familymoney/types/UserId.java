package com.familymoney.familymoney.types;

import java.util.UUID;

public record UserId(UUID value) {

  @Override
  public String toString() {
    return value.toString();
  }

  public static UserId fromString(String value) {
    return new UserId(UUID.fromString(value));
  }
}
