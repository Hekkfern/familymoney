package com.familymoney.testutils;

import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import org.apache.commons.lang3.Strings;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
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
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    Object principal = authentication.getPrincipal();
    if (!(principal instanceof UserId userId)) {
      throw new AuthenticationCredentialsNotFoundException("User ID not found in security context");
    }
    final String roleStr =
        Strings.CS.removeStart(
            authentication.getAuthorities().iterator().next().getAuthority(), ROLE_PREFIX);
    final Role role = Role.fromString(roleStr);
    return new AuthorizedUser(userId, role);
  }
}
