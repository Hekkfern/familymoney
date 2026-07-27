package com.familymoney.domains.auth.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.validations.ValidGroupName;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class GroupNameTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  record TestClass(@ValidGroupName String value) {}

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#VALID_GROUPNAMES")
  void GroupNameType_Valid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isEmpty();
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.testutils.TestDataFactory#INVALID_GROUPNAMES")
  void GroupNameType_Invalid(String str) {
    val testClass = new TestClass(str);
    val violations = validator.validate(testClass);
    assertThat(violations).isNotEmpty();
    assertThat(violations)
        .extracting(ConstraintViolation::getMessage)
        .containsExactlyInAnyOrder(
            "Name must be alphanumeric, can contain some symbols, and have a max length of 64 characters");
  }
}
