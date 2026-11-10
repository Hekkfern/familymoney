package com.familymoney.testutils;

import java.util.Map;
import java.util.stream.Collectors;

public class UserAdminControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/v1/admin/users";
  private static final String USER_PATH = BASE_PATH + "/%s";
  private static final String ENABLE_PATH = USER_PATH + "/enable?enabled=%s";
  private static final String ROLE_PATH = USER_PATH + "/role";

  // endregion

  // region Public Methods

  public static String getUsersPath(final Map<String, String> queryParams) {
    if (queryParams.isEmpty()) {
      return BASE_PATH;
    }
    final String query =
        queryParams.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    return BASE_PATH + "?" + query;
  }

  public static String getUserPath(final String userId) {
    return String.format(USER_PATH, userId);
  }

  public static String enableUserPath(final String userId, final String enabled) {
    return String.format(ENABLE_PATH, userId, enabled);
  }

  public static String userEnablePath(final String userId) {
    return String.format(USER_PATH, userId) + "/enable";
  }

  public static String userRolePath(final String userId) {
    return String.format(ROLE_PATH, userId);
  }

  // endregion
}
