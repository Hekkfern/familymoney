package com.familymoney.familymoney.unit.types;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.familymoney.familymoney.types.EmailVerificationToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class EmailVerificationTokenTests {

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#VALID_EMAILVERIFICATIONTOKENS")
  void EmailType_Valid(String str) {
    assertDoesNotThrow(() -> new EmailVerificationToken(str));
  }

  @ParameterizedTest
  @FieldSource("com.familymoney.familymoney.utils.TestDataFactory#INVALID_EMAILVERIFICATIONTOKENS")
  void EmailType_Invalid(String str) {
    assertThrows(IllegalArgumentException.class, () -> new EmailVerificationToken(str));
  }
}
