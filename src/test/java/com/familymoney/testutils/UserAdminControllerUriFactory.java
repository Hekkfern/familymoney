package com.familymoney.testutils;

public class UserAdminControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/v1/admin/users";
  private static final String USER_PATH = BASE_PATH + "/%s";

  // endregion

  // region Public Methods

  public static String getUserPath(final String userId) {
    return String.format(USER_PATH, userId);
  }

  // endregion
}
