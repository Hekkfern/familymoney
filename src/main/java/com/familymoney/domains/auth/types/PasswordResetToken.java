package com.familymoney.domains.auth.types;

import com.familymoney.testutils.RandomUtil;

public record PasswordResetToken(String value) {

  private static final int LENGTH = 64;

  public PasswordResetToken {
    if (value.length() != LENGTH) {
      throw new IllegalArgumentException("Invalid refresh token length");
    }
  }

  public static PasswordResetToken fromString(String value) {
    return new PasswordResetToken(value);
  }

  public static PasswordResetToken generate() {
    return new PasswordResetToken(RandomUtil.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
