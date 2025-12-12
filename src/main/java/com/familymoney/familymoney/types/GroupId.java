package com.familymoney.familymoney.types;

import java.util.UUID;

public record GroupId(UUID value) {

  @Override
  public String toString() {
    return value.toString();
  }

  public static GroupId fromString(String value) {
    return new GroupId(UUID.fromString(value));
  }
}
