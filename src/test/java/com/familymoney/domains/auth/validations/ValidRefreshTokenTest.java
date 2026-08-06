package com.familymoney.domains.auth.validations;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class ValidRefreshTokenTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidRefreshToken String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#VALID_REFRESHTOKENS")
  void RefreshTokenType_Valid(String str) {
    final TestClass testClass = new TestClass(str);
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_REFRESHTOKENS")
  void RefreshTokenType_Invalid(String str) {
    final TestClass testClass = new TestClass(str);
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder("Invalid token format");
  }
}
