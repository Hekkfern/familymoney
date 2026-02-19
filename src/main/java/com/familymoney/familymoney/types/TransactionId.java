package com.familymoney.familymoney.types;


import com.familymoney.familymoney.utils.UUIDGenerator;

import java.util.UUID;

public record TransactionId(UUID value) {

  public static TransactionId fromString(String value) {
    return new TransactionId(UUID.fromString(value));
  }

  public static TransactionId fromUuid(UUID value) {
    return new TransactionId(value);
  }

  public static TransactionId generate() {
    return new TransactionId(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
