package com.familymoney.familymoney.types;

import com.familymoney.familymoney.utils.RandomStringHelper;

public record RefreshToken(String value) {

  private static final int LENGTH = 32;

  public static RefreshToken fromString(String value) {
    return new RefreshToken(value);
  }

  public static RefreshToken generate() {
    return new RefreshToken(RandomStringHelper.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
