package com.familymoney.domains.transactions.validations;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.javamoney.moneta.Money;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NotNegativeMoneyValidatorTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Nested
  class IsValid {

    @Test
    void accepts_zero_money() {
      assertThat(validator.validate(new TestClass(Money.of(0, "EUR")))).isEmpty();
    }

    @Test
    void accepts_positive_money() {
      assertThat(validator.validate(new TestClass(Money.of(1, "EUR")))).isEmpty();
    }

    @Test
    void accepts_null_money() {
      assertThat(validator.validate(new TestClass(null))).isEmpty();
    }

    @Test
    void rejects_negative_money() {
      final Set<ConstraintViolation<TestClass>> violations =
          validator.validate(new TestClass(Money.of(-1, "EUR")));

      assertThat(violations)
          .extracting(ConstraintViolation::getMessage)
          .containsExactly("Money amount must be zero or positive");
    }
  }

  private record TestClass(@Nullable @NotNegativeMoney Money value) {}
}
