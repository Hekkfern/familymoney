package com.familymoney.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.HexFormat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HashHelperTest {

  @Nested
  class Sha256 {

    @ParameterizedTest
    @CsvSource({
      // Well-known SHA-256 test vector for "abc"
      "abc, ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
      // Well-known SHA-256 test vector for the empty string
      "'', e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
      // Non-ASCII input to verify UTF-8 encoding
      "ñ, 024bb90888ca89a15a19e9bdd8c712bfb070465fce1ef25e43c170ea44fc5e5f"
    })
    void returns_known_digest(final String input, final String expectedHex) {
      final byte[] result = HashHelper.sha256(input);

      assertThat(HexFormat.of().formatHex(result)).isEqualTo(expectedHex);
    }

    @Test
    void returns_same_output_for_same_input_twice() {
      final String input = "same input";

      final byte[] first = HashHelper.sha256(input);
      final byte[] second = HashHelper.sha256(input);

      assertThat(first).isEqualTo(second);
    }

    @Test
    void returns_different_output_for_different_inputs_in_consecutive_calls() {
      final byte[] first = HashHelper.sha256("first input");
      final byte[] second = HashHelper.sha256("second input");

      assertThat(first).isNotEqualTo(second);
    }

    @Test
    void rejects_null_input() {
      assertThatNullPointerException().isThrownBy(() -> HashHelper.sha256(null));
    }
  }
}
