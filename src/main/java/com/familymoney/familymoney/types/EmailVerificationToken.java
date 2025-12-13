package com.familymoney.familymoney.types;

import java.security.SecureRandom;

public record EmailVerificationToken(String value) {

  @Override
  public String toString() {
    return value;
  }

  public static EmailVerificationToken fromString(String value) {
    return new EmailVerificationToken(value);
  }

  public static EmailVerificationToken generate() {
    final int TOKEN_LENGTH = 64;
    final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
    for (int i = 0; i < TOKEN_LENGTH; i++) {
      int idx = random.nextInt(CHARSET.length());
      sb.append(CHARSET.charAt(idx));
    }
    return new EmailVerificationToken(sb.toString());
  }
}
