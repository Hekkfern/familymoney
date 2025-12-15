package com.familymoney.familymoney.types;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public record EmailVerificationToken(String value) {

  public static final int LENGTH = 64;
  private static final Pattern VALIDATION_PATTERN =
      Pattern.compile(String.format("^[A-Za-z0-9]{%s}$", EmailVerificationToken.LENGTH));

  public EmailVerificationToken {
    if (!VALIDATION_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid token format");
    }
  }

  @Override
  public String toString() {
    return value;
  }

  public static EmailVerificationToken fromString(String value) {
    return new EmailVerificationToken(value);
  }

  public static EmailVerificationToken generate() {
    final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      int idx = random.nextInt(CHARSET.length());
      sb.append(CHARSET.charAt(idx));
    }
    return new EmailVerificationToken(sb.toString());
  }
}
