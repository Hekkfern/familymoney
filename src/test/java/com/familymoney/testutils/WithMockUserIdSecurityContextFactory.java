package com.familymoney.testutils;

import com.familymoney.domains.user.types.UserId;
import java.util.List;
import lombok.val;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserIdSecurityContextFactory
    implements WithSecurityContextFactory<WithMockUserId> {

  private static final String ROLE_PREFIX = "ROLE_";

  @Override
  public SecurityContext createSecurityContext(WithMockUserId annotation) {
    val context = SecurityContextHolder.createEmptyContext();
    val userId = UserId.fromString(annotation.userId());
    val authorities = List.of(new SimpleGrantedAuthority(ROLE_PREFIX + annotation.role()));
    val auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
    context.setAuthentication(auth);
    return context;
  }
}
