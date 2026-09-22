package com.familymoney.security;

import com.familymoney.utils.HashHelper;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** SHA-256 implementation for hashing opaque bearer tokens at rest. */
@Component
public class DefaultOpaqueTokenHasher implements OpaqueTokenHasher {

  @Override
  public String hash(final String token) {
    return HexFormat.of()
        .formatHex(HashHelper.sha256(Objects.requireNonNull(token, "Token cannot be null")));
  }
}
