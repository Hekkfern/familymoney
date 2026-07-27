package com.familymoney.domains.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.users.validation.ValidPassword;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class PasswordTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidPassword String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#VALID_PASSWORDS")
  void PasswordType_Valid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_PASSWORDS")
  void PasswordType_Invalid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            "Password must be between 12 and 64 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
  }
}
