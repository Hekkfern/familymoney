package com.familymoney.familymoney.utils;

public class UserControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/users";
  private static final String ME_PATH = BASE_PATH + "/me";

  // endregion

  // region Public Methods

  public static String getMePath() {
    return ME_PATH;
  }

  // endregion
}
