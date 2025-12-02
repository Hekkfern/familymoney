package com.familymoney.familymoney.utils;

public class AuthControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/auth";
  private static final String REGISTER_PATH = BASE_PATH + "/register";
  private static final String LOGIN_PATH = BASE_PATH + "/login";
  private static final String VERIFY_EMAIL_PATH = BASE_PATH + "/verify-email/%s";
  private static final String REFRESH_PATH = BASE_PATH + "/refresh";

  // endregion

  // region Public Methods

  public static String getRegisterPath() {
    return REGISTER_PATH;
  }

  public static String getLoginPath() {
    return LOGIN_PATH;
  }

  public static String getVerifyEmailPath(String token) {
    return String.format(VERIFY_EMAIL_PATH, token);
  }

  public static String getRefreshPath() {
    return REFRESH_PATH;
  }

  // endregion
}
