package com.familymoney.testutils;

public class GroupAdminControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/v1/admin/groups";
  private static final String GROUP_PATH = BASE_PATH + "/%s";
  private static final String GROUPS_USER_PATH = BASE_PATH + "/users/%s";
  private static final String GROUP_AND_USER_PATH = BASE_PATH + "/%s/users/%s";
  private static final String GROUP_USERS_PATH = BASE_PATH + "/%s/users";

  // endregion

  // region Public Methods

  public static String getGroupsPath() {
    return BASE_PATH;
  }

  public static String getGroupPath(final String groupId) {
    return String.format(GROUP_PATH, groupId);
  }

  public static String getGroupsUserPath(final String userId) {
    return String.format(GROUPS_USER_PATH, userId);
  }

  public static String getGroupAndUserPath(final String groupId, final String userId) {
    return String.format(GROUP_AND_USER_PATH, groupId, userId);
  }

  public static String getUsersForGroupPath(final String groupId) {
    return String.format(GROUP_USERS_PATH, groupId);
  }

  // endregion
}
