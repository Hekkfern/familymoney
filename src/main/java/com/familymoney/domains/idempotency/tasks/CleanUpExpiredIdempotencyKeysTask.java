package com.familymoney.domains.idempotency.tasks;

import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanUpExpiredIdempotencyKeysTask {

  @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "CleanUpExpiredIdempotencyKeysTask")
  public void scheduleTask() {
    //TODO
  }
}
