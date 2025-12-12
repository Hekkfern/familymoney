package com.familymoney.familymoney.types;

public record Email(String value) {

  public static Email of(String value) {
    return new Email(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
