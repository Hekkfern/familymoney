package com.familymoney.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.familymoney.domains.user.validation.ValidUserName;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class UserNameTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidUserName String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.utils.TestDataFactory#VALID_USERNAMES")
  void UserNameType_Valid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertTrue(violations.isEmpty());
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.utils.TestDataFactory#INVALID_USERNAMES")
  void UserNameType_Invalid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            "Name must be alphanumeric, '-' and '_', and have a length between 3 and 32 characters");
  }
}
