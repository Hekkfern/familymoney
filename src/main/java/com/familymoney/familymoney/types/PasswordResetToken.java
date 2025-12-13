package com.familymoney.familymoney.types;

public record PasswordResetToken(String value) {

  @Override
  public String toString() {
    return value;
  }

  public static PasswordResetToken fromString(String value) {
    return new PasswordResetToken(value);
  }
}
