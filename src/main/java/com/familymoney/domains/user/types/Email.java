package com.familymoney.domains.user.types;

public record Email(String value) {

  public static Email fromString(final String value) {
    return new Email(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
