package com.familymoney.domains.auth.types;

import com.familymoney.testutils.RandomUtil;

public record EmailVerificationToken(String value) {

  private static final int LENGTH = 64;

  public EmailVerificationToken {
    if (value.length() != LENGTH) {
      throw new IllegalArgumentException("Invalid email verification token length");
    }
  }

  public static EmailVerificationToken fromString(final String value) {
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
