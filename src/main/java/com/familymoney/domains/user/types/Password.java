package com.familymoney.domains.user.types;

public record Password(String value) {

  public static Password fromString(String value) {
    return new Password(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
