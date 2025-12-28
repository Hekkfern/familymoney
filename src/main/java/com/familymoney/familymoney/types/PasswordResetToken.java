package com.familymoney.familymoney.types;

import com.familymoney.familymoney.utils.RandomStringHelper;

public record PasswordResetToken(String value) {

  private static final int LENGTH = 64;

  public static PasswordResetToken fromString(String value) {
    return new PasswordResetToken(value);
  }

  public static PasswordResetToken generate() {
    return new PasswordResetToken(RandomStringHelper.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
