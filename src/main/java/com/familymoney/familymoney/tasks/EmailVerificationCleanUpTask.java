package com.familymoney.familymoney.tasks;

import com.familymoney.familymoney.repositories.IEmailVerificationRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationCleanUpTask {

  private final IEmailVerificationRepository emailVerificationRepository;

  private static final Duration DURATION_THRESHOLD_FOR_CLEANUP = Duration.ofDays(7);

  @Scheduled(cron = "@daily", zone = "UTC")
  public void execute() {
    emailVerificationRepository.deleteOlderThan(DURATION_THRESHOLD_FOR_CLEANUP);
  }
}
