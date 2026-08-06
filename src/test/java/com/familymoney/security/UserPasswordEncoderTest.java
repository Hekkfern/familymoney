package com.familymoney.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserPasswordEncoderTest {

  @Nested
  class Encode {

    @Test
    void returns_a_hash_that_verifies_the_raw_password() {
      final UserPasswordEncoder passwordEncoder = new UserPasswordEncoder();
      final String password = FakeGenerator.password();

      final String encodedPassword = passwordEncoder.encode(password);

      assertThat(encodedPassword).isNotEqualTo(password);
      assertThat(passwordEncoder.verify(password, encodedPassword)).isTrue();
    }
  }

  @Nested
  class Verify {

    @Test
    void returns_true_when_raw_password_matches_hash() {
      final UserPasswordEncoder passwordEncoder = new UserPasswordEncoder();
      final String password = FakeGenerator.password();
      final String encodedPassword = passwordEncoder.encode(password);

      assertThat(passwordEncoder.verify(password, encodedPassword)).isTrue();
    }

    @Test
    void returns_false_when_raw_password_does_not_match_hash() {
      final UserPasswordEncoder passwordEncoder = new UserPasswordEncoder();
      final String encodedPassword = passwordEncoder.encode(FakeGenerator.password());

      assertThat(passwordEncoder.verify(FakeGenerator.password(), encodedPassword)).isFalse();
    }
  }

  @Nested
  class VerifyDummyPassword {

    @Test
    void returns_false_for_an_arbitrary_password() {
      final UserPasswordEncoder passwordEncoder = new UserPasswordEncoder();

      assertThat(passwordEncoder.verifyDummyPassword(FakeGenerator.password())).isFalse();
    }
  }
}
