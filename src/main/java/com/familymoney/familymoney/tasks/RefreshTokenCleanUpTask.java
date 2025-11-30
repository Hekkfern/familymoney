package com.familymoney.familymoney.tasks;

import com.familymoney.familymoney.repositories.IRefreshTokenRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class RefreshTokenCleanUpTask {

  private final IRefreshTokenRepository refreshTokenRepository;

  private final Duration DURATION_THRESHOLD_FOR_CLEANUP = Duration.ofDays(30);

  @Scheduled(cron = "@daily", zone = "UTC")
  void execute() {
    refreshTokenRepository.deleteOlderThan(DURATION_THRESHOLD_FOR_CLEANUP);
  }
}
