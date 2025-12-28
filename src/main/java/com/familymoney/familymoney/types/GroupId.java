package com.familymoney.familymoney.types;

import java.util.UUID;

public record GroupId(UUID value) {

  public static GroupId fromString(String value) {
    return new GroupId(UUID.fromString(value));
  }

  public static GroupId fromUuid(UUID value) {
    return new GroupId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
