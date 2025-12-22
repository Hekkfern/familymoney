package com.familymoney.familymoney.unit.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.familymoney.familymoney.types.UserName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class UserNameTests {
  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#VALID_USERNAMES")
  void EmailType_Valid(String str) {
    assertDoesNotThrow(() -> new UserName(str));
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_USERNAMES")
  void EmailType_Invalid(String str) {
    assertThrows(IllegalArgumentException.class, () -> new UserName(str));
  }
}
