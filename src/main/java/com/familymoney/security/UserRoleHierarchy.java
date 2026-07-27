package com.familymoney.security;

import com.familymoney.domains.users.types.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Component;

@Component
public class UserRoleHierarchy {

  private UserRoleHierarchy() {
    /* This utility class should not be instantiated */
  }

  @Bean
  static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role(Role.ADMIN.toString())
        .implies(Role.USER.toString())
        .build();
  }
}
