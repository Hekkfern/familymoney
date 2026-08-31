package com.familymoney.domains.admin.init;

import com.familymoney.domains.users.services.UserService;
import com.familymoney.properties.AdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Application runner that creates the first admin user on application startup.
 *
 * <p>This runner checks if an admin user already exists and creates one using the configured
 * credentials if not. It ensures that there is always at least one default admin user available
 * after deployment.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FirstAdminUserRunner implements ApplicationRunner {

  private final UserService userService;
  private final AdminProperties adminProperties;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("Creating first admin user...");
    userService.createAdminUser(
        adminProperties.username(), adminProperties.email(), adminProperties.password());
  }
}
