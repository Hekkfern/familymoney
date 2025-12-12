package com.familymoney.familymoney.init;

import com.familymoney.familymoney.properties.AdminProperties;
import com.familymoney.familymoney.services.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FirstAdminUserRunner implements ApplicationRunner {

  private final IUserService userService;
  private final AdminProperties adminProperties;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("Creating first admin user...");
    userService.createAdminUser(
        adminProperties.username(), adminProperties.email(), adminProperties.password());
  }
}
