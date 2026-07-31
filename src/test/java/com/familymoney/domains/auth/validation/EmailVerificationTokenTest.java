package com.familymoney.domains.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class EmailVerificationTokenTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidEmailVerificationToken String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#VALID_EMAILVERIFICATIONTOKENS")
  void EmailVerificationTokenType_Valid(String str) {
    final TestClass testClass = new TestClass(str);
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_EMAILVERIFICATIONTOKENS")
  void EmailVerificationTokenType_Invalid(String str) {
    final TestClass testClass = new TestClass(str);
    final Set<ConstraintViolation<TestClass>> violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder("Invalid token format");
  }
}
