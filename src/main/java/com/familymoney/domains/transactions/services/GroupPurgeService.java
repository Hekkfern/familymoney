package com.familymoney.domains.transactions.services;

/**
 * Service that physically removes soft-deleted groups, together with their dependent rows, once
 * their retention period has elapsed.
 */
public interface GroupPurgeService {

  /**
   * Executes one purge pass. Each dependent table of the soft-deleted groups is purged with, at
   * most, one batch, following the dependency order, and finally the groups without remaining
   * dependent rows are deleted. Every batch is committed independently.
   *
   * @return the total number of rows deleted during the pass; {@code 0} when nothing remains to be
   *     purged
   */
  int purgeDeletedGroupsBatch();
}
