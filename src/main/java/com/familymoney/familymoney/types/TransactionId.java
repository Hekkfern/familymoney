package com.familymoney.familymoney.types;

import java.util.UUID;

public record TransactionId(UUID value) {

  @Override
  public String toString() {
    return value.toString();
  }

  public static TransactionId fromString(String value) {
    return new TransactionId(UUID.fromString(value));
  }
}
