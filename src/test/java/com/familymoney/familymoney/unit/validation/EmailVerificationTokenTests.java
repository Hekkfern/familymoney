package com.familymoney.familymoney.unit.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.familymoney.familymoney.validation.ValidEmailVerificationToken;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class EmailVerificationTokenTests {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidEmailVerificationToken String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#VALID_EMAILVERIFICATIONTOKENS")
  void EmailType_Valid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertTrue(violations.isEmpty());
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_EMAILVERIFICATIONTOKENS")
  void EmailType_Invalid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder("Invalid token format");
  }
}
