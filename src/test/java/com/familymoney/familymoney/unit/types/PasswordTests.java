package com.familymoney.familymoney.unit.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.familymoney.familymoney.types.Password;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class PasswordTests {

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#VALID_PASSWORDS")
  void EmailType_Valid(String str) {
    assertDoesNotThrow(() -> new Password(str));
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_PASSWORDS")
  void EmailType_Invalid(String str) {
    assertThrows(IllegalArgumentException.class, () -> new Password(str));
  }
}
