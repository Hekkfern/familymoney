package com.familymoney.familymoney.unit.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.familymoney.familymoney.types.Username;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class UsernameTests {
  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#VALID_USERNAMES")
  void EmailType_Valid(String str) {
    assertDoesNotThrow(() -> new Username(str));
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_USERNAMES")
  void EmailType_Invalid(String str) {
    assertThrows(IllegalArgumentException.class, () -> new Username(str));
  }
}
