package com.familymoney.familymoney.types;

import java.security.SecureRandom;
import org.jspecify.annotations.NonNull;

public record EmailVerificationToken(String value) {

  @Override
  public @NonNull String toString() {
    return value;
  }

  @NonNull
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
