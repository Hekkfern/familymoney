package com.familymoney.familymoney.unit.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.familymoney.familymoney.types.Email;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class EmailTests {

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#VALID_EMAILS")
  void EmailType_Valid(String str) {
    assertDoesNotThrow(() -> new Email(str));
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_EMAILS")
  void EmailType_Invalid(String str) {
    assertThrows(IllegalArgumentException.class, () -> new Email(str));
  }
}
