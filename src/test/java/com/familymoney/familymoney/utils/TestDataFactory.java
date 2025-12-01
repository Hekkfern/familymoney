package com.familymoney.familymoney.utils;

import java.util.List;

public final class TestDataFactory {

  public static List<String> VALID_USERNAMES =
      List.of("user_123", "john-doe", "alice99", "bob_smith", "charlie01");

  public static List<String> INVALID_USERNAMES =
      List.of(
          "",
          "a",
          "ab",
          "1abc",
          "-abc",
          "_abc",
          "Abc",
          "aBc",
          "a21354.asd",
          "user name",
          "user@name",
          "user,name",
          "user$money",
          "thisusernameiswaytoolongtobevalid2s1af54saf54s5daf6s541f65as1");

  public static List<String> VALID_EMAILS =
      List.of(
          "hector.fernandez+dev@example.com",
          "user+tag@example.co.uk",
          "john.doe@example.com",
          "long.user@example-domain.com",
          "alpha1@mail.example.org");

  public static List<String> INVALID_EMAILS =
      List.of(
          "",
          "plainaddress",
          "@no-local-part.com",
          "no-at.domain.com",
          "user@.com",
          "user@domain..com",
          "user@@domain.com",
          "user@domain,com",
          " user@domain.com");

  public static List<String> VALID_PASSWORDS =
      List.of("StrongPass1!", "Aa1$aaaaaaaa", "Password123$!", "Zz9@aaaaaaaaaaa", "GoodPass1@$a");

  public static List<String> INVALID_PASSWORDS =
      List.of(
          "Short1!",
          "alllowercase1!",
          "ALLUPPERCASE1!",
          "NoDigitPassword!",
          "NoSpecialChar1A",
          "Invalid#Char1A",
          "Contains Space1!",
          "\tTabInPassword1!",
          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
}
