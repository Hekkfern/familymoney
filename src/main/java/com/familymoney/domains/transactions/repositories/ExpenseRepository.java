package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * Repository interface that defines persistence operations for expenses.
 *
 * <p>Implementations of this interface are responsible for creating, updating and deleting
 * expenses, resolving transaction details, and paging queries for expenses associated with a group.
 *
 * <p>All methods return Domain-specific DB objects (Dbo) or primitives that indicate success.
 * Optional is used for methods that may not find or create a resource.
 */
public interface ExpenseRepository {

  /**
   * Creates a new transaction record with the provided details.
   *
   * @param dto values to store
   */
  void create(CreateExpenseDto dto);

  /**
   * Updates the transaction record identified by the given TransactionId with the provided data.
   * Only non-null fields of {@code data} should be applied.
   *
   * @param id the identifier of the transaction to be updated
   * @param dto the data containing the updated information for the transaction. Non-null fields in
   *     the {@link UpdateExpenseDto} will be used to update the corresponding fields in the
   *     transaction record.
   */
  void updateById(ExpenseId id, UpdateExpenseDto dto);

  /**
   * Deletes the transaction record identified by the given TransactionId.
   *
   * @param id the identifier of the transaction to be deleted
   */
  void deleteById(ExpenseId id);

  /**
   * Retrieves a transaction record by its unique identifier.
   *
   * @param id the identifier of the transaction to retrieve
   * @return an {@link Optional} containing the {@link FullExpenseEntity} if found, otherwise empty.
   */
  Optional<FullExpenseEntity> findById(ExpenseId id);

  /**
   * Retrieves a paginated list of expenses associated with a specific group.
   *
   * @param groupId the identifier of the group for which to retrieve expenses
   * @return a page of {@link FullExpenseEntity} objects representing expenses associated with the
   *     specified group. If no expenses are found for the group, the returned page will be empty.
   */
  Page<FullExpenseEntity> findAllByGroupId(GroupId groupId, int page, int size);
}
