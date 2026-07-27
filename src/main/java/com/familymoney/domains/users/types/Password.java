package com.familymoney.domains.users.types;

public record Password(String value) {

  public static Password fromString(final String value) {
    return new Password(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
