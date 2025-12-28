package com.familymoney.familymoney.types;

import java.util.UUID;

public record TransactionId(UUID value) {

  public static TransactionId fromString(String value) {
    return new TransactionId(UUID.fromString(value));
  }

  public static TransactionId fromUuid(UUID value) {
    return new TransactionId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
