package com.familymoney.testutils;

public class UserControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/v1/users";
  private static final String ME_PATH = BASE_PATH + "/me";

  // endregion

  // region Public Methods

  public static String getMePath() {
    return ME_PATH;
  }

  // endregion
}
