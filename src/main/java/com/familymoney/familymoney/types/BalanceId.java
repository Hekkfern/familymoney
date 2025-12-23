package com.familymoney.familymoney.types;

import java.util.UUID;

public record BalanceId(UUID value) {

  @Override
  public String toString() {
    return value.toString();
  }

  public static BalanceId fromString(String value) {
    return new BalanceId(UUID.fromString(value));
  }
}
