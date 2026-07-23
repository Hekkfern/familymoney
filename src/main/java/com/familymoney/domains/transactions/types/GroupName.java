package com.familymoney.domains.transactions.types;

public record GroupName(String value) {

  public GroupName {
    if (value.isBlank()) {
      throw new IllegalArgumentException("Invalid group name");
    }
  }

  public static GroupName fromString(String value) {
    return new GroupName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
