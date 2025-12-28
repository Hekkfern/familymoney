package com.familymoney.familymoney.types;

import com.familymoney.familymoney.utils.RandomStringHelper;

public record EmailVerificationToken(String value) {

  private static final int LENGTH = 64;

  public static EmailVerificationToken fromString(String value) {
    return new EmailVerificationToken(value);
  }

  public static EmailVerificationToken generate() {
    return new EmailVerificationToken(RandomStringHelper.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
