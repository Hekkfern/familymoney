package com.familymoney.familymoney.types;

import com.familymoney.familymoney.utils.RandomStringHelper;

public record ShareGroupToken(String value) {

  private static final int LENGTH = 64;

  public static ShareGroupToken fromString(String value) {
    return new ShareGroupToken(value);
  }

  public static ShareGroupToken generate() {
    return new ShareGroupToken(RandomStringHelper.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
