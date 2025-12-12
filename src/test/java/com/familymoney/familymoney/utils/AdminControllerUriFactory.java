package com.familymoney.familymoney.utils;

import com.familymoney.familymoney.types.UserId;

public class AdminControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/admin/users";

  // endregion

  // region Public Methods

  public static String getUserPath(UserId userId) {
    return BASE_PATH + "/%s";
  }

  // endregion
}
