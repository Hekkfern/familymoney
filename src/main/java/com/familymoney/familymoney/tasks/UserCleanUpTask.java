package com.familymoney.familymoney.tasks;

import com.familymoney.familymoney.repositories.IUserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class UserCleanUpTask {

  private final IUserRepository userRepository;

  private final Duration DURATION_THRESHOLD_FOR_CLEANUP = Duration.ofDays(7);

  @Scheduled(cron = "@daily", zone = "UTC")
  void execute() {
    userRepository.deleteByIsUnverifiedAndOlderThan(DURATION_THRESHOLD_FOR_CLEANUP);
  }
}
