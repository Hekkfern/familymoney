package com.familymoney.domains.transactions.types;

import com.familymoney.utils.UUIDGenerator;
import java.util.UUID;

public record ExpenseId(UUID value) {

  public static ExpenseId fromString(String value) {
    return new ExpenseId(UUID.fromString(value));
  }

  public static ExpenseId fromUuid(UUID value) {
    return new ExpenseId(value);
  }

  public static ExpenseId generate() {
    return new ExpenseId(UUIDGenerator.generate());
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
