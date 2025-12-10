package com.familymoney.familymoney.utils;

import com.familymoney.familymoney.types.UserId;
import org.jspecify.annotations.NonNull;

public class AdminControllerUriFactory {

  // region Constants

  private static final String BASE_PATH = "/api/admin/users";

  // endregion

  // region Public Methods

  public static String getUserPath(@NonNull UserId userId) {
    return BASE_PATH + "/%s";
  }

  // endregion
}
