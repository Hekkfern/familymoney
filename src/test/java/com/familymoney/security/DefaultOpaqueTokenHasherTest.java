package com.familymoney.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DefaultOpaqueTokenHasherTest {

  private final DefaultOpaqueTokenHasher tokenHasher = new DefaultOpaqueTokenHasher();

  @Nested
  class Hash {

    @Test
    void returns_sha256_digest_as_lowercase_hexadecimal() {
      assertThat(tokenHasher.hash("hello"))
          .isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
    }

    @Test
    void returns_same_digest_for_same_token() {
      assertThat(tokenHasher.hash("token")).isEqualTo(tokenHasher.hash("token"));
    }

    @Test
    void returns_different_digests_for_different_tokens() {
      assertThat(tokenHasher.hash("token-one")).isNotEqualTo(tokenHasher.hash("token-two"));
    }

    @Test
    void rejects_null_token() {
      assertThatNullPointerException().isThrownBy(() -> tokenHasher.hash(null));
    }
  }
}
