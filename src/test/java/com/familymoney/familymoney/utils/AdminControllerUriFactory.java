package com.familymoney.familymoney.utils;

import com.familymoney.familymoney.types.UserId;

public class AdminControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/v1/admin/users";
  private static final String USER_PATH = BASE_PATH + "/%s";

  // endregion

  // region Public Methods

  public static String getUserPath(UserId userId) {
    return String.format(USER_PATH, userId);
  }

  // endregion
}
