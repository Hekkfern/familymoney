package com.familymoney.types;

public record Email(String value) {

  public static Email fromString(String value) {
    return new Email(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
