package com.familymoney.domains.transactions.types;

import com.familymoney.utils.UUIDGenerator;
import java.util.UUID;

public record GroupId(UUID value) {

  public static GroupId fromString(String value) {
    return new GroupId(UUID.fromString(value));
  }

  public static GroupId fromUuid(UUID value) {
    return new GroupId(value);
  }

  public static GroupId generate() {
    return new GroupId(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
