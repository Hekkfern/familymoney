package com.familymoney.familymoney.utils;

public class RandomStringHelper {

  public static String generateRandomString(int length) {
    final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(length);
    java.security.SecureRandom random = new java.security.SecureRandom();
    for (int i = 0; i < length; i++) {
      int idx = random.nextInt(CHARSET.length());
      sb.append(CHARSET.charAt(idx));
    }
    return sb.toString();
  }
}
