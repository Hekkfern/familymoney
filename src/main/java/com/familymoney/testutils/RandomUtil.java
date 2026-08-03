package com.familymoney.testutils;

import java.security.SecureRandom;

public class RandomUtil {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final String ALPHANUMERIC =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  private RandomUtil() {
    /* This utility class should not be instantiated */
  }

  public static String generateRandomString(final int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      final int idx = SECURE_RANDOM.nextInt(ALPHANUMERIC.length());
      sb.append(ALPHANUMERIC.charAt(idx));
    }
    return sb.toString();
  }
}
