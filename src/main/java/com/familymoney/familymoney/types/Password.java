package com.familymoney.familymoney.types;

public record Password(String value) {

  public static Password of(String value) {
    return new Password(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
