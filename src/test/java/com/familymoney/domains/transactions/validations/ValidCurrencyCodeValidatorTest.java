package com.familymoney.domains.transactions.validations;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidCurrencyCodeValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidCurrencyCode @Nullable String value) {}

  @Nested
  class IsValid {

    @ParameterizedTest
    @ValueSource(strings = {"EUR", "USD", "GBP", "JPY"})
    void accepts_valid_iso_codes(final String code) {
      final Set<ConstraintViolation<TestClass>> violations =
          validator.validate(new TestClass(code));

      assertThat(violations).isEmpty();
    }

    @Test
    void accepts_null_value() {
      final Set<ConstraintViolation<TestClass>> violations =
          validator.validate(new TestClass(null));

      assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ZZZ", "eur", "EU", "EURO", "12A", " EUR"})
    void rejects_invalid_codes(final String code) {
      final Set<ConstraintViolation<TestClass>> violations =
          validator.validate(new TestClass(code));

      assertThat(violations).hasSize(1);
    }
  }
}
