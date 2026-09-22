package com.familymoney.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashHelper {

  private static final String SHA_256 = "SHA-256";

  private HashHelper() {
    /* This utility class should not be instantiated */
  }

  public static byte[] sha256(final String value) {
    try {
      final MessageDigest digest = MessageDigest.getInstance(SHA_256);
      return digest.digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (final NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
