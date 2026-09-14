package com.familymoney.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jooq.JooqLockProvider;
import net.javacrumbs.shedlock.spring.SpringNewTransactionRunner;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m", defaultLockAtLeastFor = "60s")
public class SchedulerConfig {

  @Bean
  public LockProvider getLockProvider(
      DSLContext dslContext, PlatformTransactionManager transactionManager) {
    return new JooqLockProvider(dslContext, new SpringNewTransactionRunner(transactionManager));
  }
}
