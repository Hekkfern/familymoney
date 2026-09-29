package com.familymoney.domains.transactions.types;

public record GroupName(String value) {

  private static final int MAX_LENGTH = 64;

  public GroupName {
    final boolean isInvalid = value.isBlank() || value.length() > MAX_LENGTH;
    if (isInvalid) {
      throw new IllegalArgumentException("Invalid group name");
    }
  }

  public static GroupName fromString(final String value) {
    return new GroupName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
