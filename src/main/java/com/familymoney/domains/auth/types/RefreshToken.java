package com.familymoney.domains.auth.types;

import com.familymoney.utils.RandomUtil;

public record RefreshToken(String value) {

  private static final int LENGTH = 32;

  public RefreshToken {
    if (value.length() != LENGTH) {
      throw new IllegalArgumentException("Invalid refresh token length");
    }
  }

  public static RefreshToken fromString(final String value) {
    return new RefreshToken(value);
  }

  public static RefreshToken generate() {
    return new RefreshToken(RandomUtil.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
