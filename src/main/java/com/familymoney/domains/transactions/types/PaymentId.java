package com.familymoney.domains.transactions.types;

import com.familymoney.utils.UUIDGenerator;
import java.util.UUID;

public record PaymentId(UUID value) {

  public static PaymentId fromString(String value) {
    return new PaymentId(UUID.fromString(value));
  }

  public static PaymentId fromUuid(UUID value) {
    return new PaymentId(value);
  }

  public static PaymentId generate() {
    return new PaymentId(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
