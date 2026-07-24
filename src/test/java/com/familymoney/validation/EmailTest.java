package com.familymoney.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.familymoney.domains.user.validation.ValidEmail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class EmailTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidEmail String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#VALID_EMAILS")
  void EmailType_Valid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertTrue(violations.isEmpty());
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILS")
  void EmailType_Invalid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder("Invalid email format");
  }
}
