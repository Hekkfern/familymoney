package com.familymoney.domains.auth.types;

import com.familymoney.testutils.RandomUtil;

public record EmailVerificationToken(String value) {

  private static final int LENGTH = 64;

  public static EmailVerificationToken fromString(String value) {
    return new EmailVerificationToken(value);
  }

  public static EmailVerificationToken generate() {
    return new EmailVerificationToken(RandomUtil.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
