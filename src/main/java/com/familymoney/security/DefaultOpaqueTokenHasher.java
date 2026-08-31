package com.familymoney.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** SHA-256 implementation for hashing opaque bearer tokens at rest. */
@Component
public class DefaultOpaqueTokenHasher implements OpaqueTokenHasher {

  private static final String ALGORITHM = "SHA-256";

  @Override
  public String hash(final String token) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance(ALGORITHM)
                  .digest(
                      Objects.requireNonNull(token, "Token cannot be null")
                          .getBytes(StandardCharsets.UTF_8)));
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
