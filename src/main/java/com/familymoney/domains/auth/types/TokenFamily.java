package com.familymoney.domains.auth.types;

import java.util.UUID;

public record TokenFamily(UUID value) {

  public static TokenFamily fromString(String value) {
    return new TokenFamily(UUID.fromString(value));
  }

  public static TokenFamily fromUuid(UUID value) {
    return new TokenFamily(value);
  }

  public static TokenFamily generate() {
    return new TokenFamily(UUID.randomUUID());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
