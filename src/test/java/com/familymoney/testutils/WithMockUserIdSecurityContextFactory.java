package com.familymoney.testutils;

import com.familymoney.domains.users.types.UserId;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserIdSecurityContextFactory
    implements WithSecurityContextFactory<WithMockUserId> {

  private static final String ROLE_PREFIX = "ROLE_";

  @Override
  public SecurityContext createSecurityContext(WithMockUserId annotation) {
    final SecurityContext context = SecurityContextHolder.createEmptyContext();
    final UserId userId = UserId.fromString(annotation.userId());
    final List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority(ROLE_PREFIX + annotation.role()));
    final UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(userId, null, authorities);
    context.setAuthentication(auth);
    return context;
  }
}
