package com.familymoney.domains.transactions.types;

import com.familymoney.testutils.UUIDGenerator;
import java.util.UUID;

public record BalanceId(UUID value) {

  public static BalanceId fromString(String value) {
    return new BalanceId(UUID.fromString(value));
  }

  public static BalanceId fromUuid(UUID value) {
    return new BalanceId(value);
  }

  public static BalanceId generate() {
    return new BalanceId(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
