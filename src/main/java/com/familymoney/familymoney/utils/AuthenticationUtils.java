package com.familymoney.familymoney.utils;

import com.familymoney.familymoney.types.Role;
import com.familymoney.familymoney.types.UserId;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtils {

  private static final String ROLE_PREFIX = "ROLE_";

  private AuthenticationUtils() {
    /* This utility class should not be instantiated */
  }

  /**
   * Helper to safely extract UserId from SecurityContext and throw a consistent exception if
   * missing
   *
   * @return ID of the currently authenticated user, and their role
   */
  public static AuthorizedUser getUserIdFromSecurityContext()
      throws AuthenticationCredentialsNotFoundException {
    val authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    val roleStr =
        Strings.CS.removeStart(
            authentication.getAuthorities().iterator().next().getAuthority(), ROLE_PREFIX);
    val role = Role.fromString(roleStr);
    return AuthorizedUser.builder().id(userId).role(role).build();
  }
}
