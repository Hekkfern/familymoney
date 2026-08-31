package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateTransactionDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateTransactionDto;
import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.TransactionId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface that defines persistence operations for transactions.
 *
 * <p>Implementations of this interface are responsible for creating, updating and deleting
 * transactions, resolving transaction details, and paging queries for transactions associated with
 * a group.
 *
 * <p>All methods return Domain-specific DB objects (Dbo) or primitives that indicate success.
 * Optional is used for methods that may not find or create a resource.
 */
public interface TransactionRepository {

  /**
   * Creates a new transaction record with the provided details.
   *
   * @param data values to store
   * @return an {@link Optional} containing the created TransactionDbo if the creation was
   *     successful, or an empty {@link Optional} if the creation failed (e.g., due to invalid input
   *     or database constraints).
   */
  Optional<TransactionEntity> create(CreateTransactionDto data);

  /**
   * Updates the transaction record identified by the given TransactionId with the provided data.
   * Only non-null fields of {@code data} should be applied.
   *
   * @param id the identifier of the transaction to be updated
   * @param data the data containing the updated information for the transaction. Non-null fields in
   *     the {@link UpdateTransactionDto} will be used to update the corresponding fields in the
   *     transaction record.
   * @return true if the transaction was updated (record existed and changes were applied), false
   *     otherwise.
   */
  boolean updateById(TransactionId id, UpdateTransactionDto data);

  /**
   * Deletes the transaction record identified by the given TransactionId.
   *
   * @param id the identifier of the transaction to be deleted
   * @return true if the transaction was deleted (record existed and was removed), false if no
   *     transaction with the given id existed or the deletion failed.
   */
  boolean deleteById(TransactionId id);

  /**
   * Retrieves a transaction record by its unique identifier.
   *
   * @param id the identifier of the transaction to retrieve
   * @return an {@link Optional} containing the TransactionDbo if found, otherwise empty.
   */
  Optional<TransactionEntity> findById(TransactionId id);

  /**
   * Retrieves a paginated list of transactions associated with a specific group.
   *
   * @param groupId the identifier of the group for which to retrieve transactions
   * @param pageable paging information (page number, size, sort).
   * @return a page of TransactionDbo objects representing transactions associated with the
   *     specified group. If no transactions are found for the group, the returned page will be
   *     empty.
   */
  Page<TransactionEntity> findAllByGroupId(GroupId groupId, Pageable pageable);
}
