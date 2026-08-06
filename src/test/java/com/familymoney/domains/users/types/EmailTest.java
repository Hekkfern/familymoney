package com.familymoney.domains.users.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Locale;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EmailTest {

  @Nested
  class FromString {

    @Test
    void rejects_null_email() {
      assertThatNullPointerException().isThrownBy(() -> Email.fromString(null));
    }

    @Test
    void rejects_email_longer_than_254_characters() {
      final String email = "a".repeat(243) + "@example.com";

      assertThatIllegalArgumentException().isThrownBy(() -> Email.fromString(email));
    }

    @Test
    void accepts_email_with_254_characters() {
      final String email = "a".repeat(242) + "@example.com";

      assertThat(Email.fromString(email).value()).isEqualTo(email);
    }

    @Test
    void canonicalizes_email_using_root_locale() {
      final Locale originalLocale = Locale.getDefault();
      Locale.setDefault(Locale.forLanguageTag("tr-TR"));
      try {
        assertThat(Email.fromString("I@EXAMPLE.COM").value()).isEqualTo("i@example.com");
      } finally {
        Locale.setDefault(originalLocale);
      }
    }
  }
}
