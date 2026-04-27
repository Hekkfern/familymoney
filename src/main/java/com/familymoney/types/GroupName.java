package com.familymoney.types;

public record GroupName(String value) {

  public static GroupName fromString(String value) {
    return new GroupName(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
