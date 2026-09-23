package com.familymoney.domains.idempotency.tasks;

import com.familymoney.domains.idempotency.repositories.IdempotencyRepository;
import com.familymoney.properties.IdempotencyProperties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanUpExpiredIdempotencyKeysTask {

  private final IdempotencyProperties idempotencyProperties;
  private final IdempotencyRepository idempotencyRepository;

  @Scheduled(fixedRate = 5, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "CleanUpExpiredIdempotencyKeysTask")
  public void scheduleTask() {
    idempotencyRepository.deleteExpired(idempotencyProperties.deleteBatchSize());
  }
}
