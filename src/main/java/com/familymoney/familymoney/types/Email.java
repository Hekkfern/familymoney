package com.familymoney.familymoney.types;

public record Email(String value) {

  @Override
  public String toString() {
    return value;
  }

  public static Email fromString(String value) {
    return new Email(value);
  }
}
