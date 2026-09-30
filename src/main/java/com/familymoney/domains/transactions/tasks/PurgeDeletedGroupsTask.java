package com.familymoney.domains.transactions.tasks;

import com.familymoney.domains.transactions.services.GroupPurgeService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that purges soft-deleted groups, and their dependent rows, in small batches until
 * nothing remains to be purged.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PurgeDeletedGroupsTask {

  private static final long FIXED_DELAY_MINUTES = 1440; // 24 hours
  private static final long INITIAL_DELAY_MINUTES = 30;
  private static final String LOCK_AT_MOST_FOR = "1h";

  private final GroupPurgeService groupPurgeService;

  /** Runs purge passes until a pass deletes no rows, and logs the total number of deleted rows. */
  @Scheduled(
      fixedDelay = FIXED_DELAY_MINUTES,
      initialDelay = INITIAL_DELAY_MINUTES,
      timeUnit = TimeUnit.MINUTES)
  @SchedulerLock(name = "PurgeDeletedGroupsTask", lockAtMostFor = LOCK_AT_MOST_FOR)
  public void scheduleTask() {
    final long totalDeletedRows =
        IntStream.generate(groupPurgeService::purgeDeletedGroupsBatch)
            .takeWhile(deletedRows -> deletedRows > 0)
            .asLongStream()
            .sum();
    log.info("Purge of soft-deleted groups finished, {} rows deleted", totalDeletedRows);
  }
}
