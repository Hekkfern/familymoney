package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.entitites.TransactionReference;
import com.familymoney.domains.transactions.types.GroupId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Provides paginated references to group transactions. */
public interface TransactionRepository {

  /**
   * Finds expenses and payments ordered by completion time descending, type ascending, and ID
   * descending.
   *
   * @param groupId the group whose transactions to retrieve
   * @param pageable the page size and offset; its sort is not used
   * @return the transaction references and total number of group transactions
   */
  Page<TransactionReference> findAllByGroupId(final GroupId groupId, final Pageable pageable);
}
