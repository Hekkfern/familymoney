package com.familymoney.familymoney.types;

public record Username(String value) {

  public static Username of(String value) {
    return new Username(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
