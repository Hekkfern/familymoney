package com.familymoney.familymoney.utils;

import com.familymoney.familymoney.types.UserId;
import lombok.val;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtils {

  /**
   * Helper to safely extract UserId from SecurityContext and throw a consistent exception if
   * missing
   *
   * @return ID of the currently authenticated user
   */
  public static UserId getUserIdFromSecurityContext() {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    return userId;
  }
}
