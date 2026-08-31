package com.familymoney.security;

/** Hashes opaque bearer tokens before they are persisted or used in persistence lookups. */
public interface OpaqueTokenHasher {

  /**
   * Returns the lowercase hexadecimal SHA-256 digest of an opaque token.
   *
   * @param token raw opaque token
   * @return deterministic 64-character lowercase hexadecimal digest
   */
  String hash(String token);
}
