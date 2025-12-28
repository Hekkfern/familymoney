package com.familymoney.familymoney.types;

import java.util.UUID;

public record BalanceId(UUID value) {

  public static BalanceId fromString(String value) {
    return new BalanceId(UUID.fromString(value));
  }

  public static BalanceId fromUuid(UUID value) {
    return new BalanceId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
