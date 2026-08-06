package com.familymoney.domains.users.validations;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class ValidEmailTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidEmail String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#VALID_EMAILS")
  void accepts_valid_email(String str) {
    final TestClass testClass = new TestClass(str);
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILS")
  void rejects_invalid_email(String str) {
    final TestClass testClass = new TestClass(str);
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .contains("Invalid email format");
  }

  @Test
  void accepts_email_with_254_characters() {
    final String validEmail = validEmailWithLength(254);

    final Set<ConstraintViolation<TestClass>> violations =
        validator.validate(new TestClass(validEmail));

    assertThat(violations).isEmpty();
  }

  @Test
  void rejects_email_with_255_characters() {
    final String invalidEmail = validEmailWithLength(255);

    final Set<ConstraintViolation<TestClass>> violations =
        validator.validate(new TestClass(invalidEmail));

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder("Invalid email size");
  }

  @Test
  void rejects_email_with_fewer_than_4_characters() {
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(new TestClass("abc"));

    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder("Invalid email format", "Invalid email size");
  }

  private String validEmailWithLength(final int length) {
    final String localPart = "a".repeat(64);
    final String firstDomainLabel = "b".repeat(63);
    final String secondDomainLabel = "c".repeat(63);
    final int finalDomainLabelLength =
        length - localPart.length() - firstDomainLabel.length() - secondDomainLabel.length() - 3;
    return localPart
        + "@"
        + firstDomainLabel
        + "."
        + secondDomainLabel
        + "."
        + "d".repeat(finalDomainLabelLength);
  }
}
