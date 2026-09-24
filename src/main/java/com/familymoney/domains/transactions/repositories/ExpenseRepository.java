package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreateExpenseException;
import com.familymoney.domains.transactions.repositories.exceptions.DeleteExpenseException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdateExpenseException;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * Repository interface that defines persistence operations for expenses.
 *
 * <p>An expense records a total amount owed by a group, split across each member's {@code share}
 * of it, together with the amounts individually paid by each {@code payer} toward it. Both are
 * recorded in the same currency; implementations are responsible for enforcing that shares and
 * payers use a single, consistent currency and that their totals match.
 *
 * <p>Implementations are responsible for creating, updating, and deleting expenses, and for paged
 * queries of expenses associated with a group. Optional is used for methods that may not find a
 * resource.
 */
public interface ExpenseRepository {

  /**
   * Creates a new expense with the provided details.
   *
   * @param dto values to store
   * @throws CreateExpenseException if the expense could not be created, or if its currency does
   *     not match the currency of its group
   */
  void create(CreateExpenseDto dto);

  /**
   * Updates the expense identified by {@code id} with the provided data. Only non-null fields of
   * {@code dto} are applied.
   *
   * @param id the identifier of the expense to update
   * @param dto the data containing the updated information for the expense. Non-null fields in
   *     the {@link UpdateExpenseDto} are used to update the corresponding fields of the expense.
   * @throws UpdateExpenseException if no expense with the given ID exists, if the updated shares
   *     or payers are empty, non-positive, or use a currency other than the expense's own, or if
   *     the shares and payers totals would no longer match
   */
  void updateById(ExpenseId id, UpdateExpenseDto dto);

  /**
   * Deletes the expense identified by {@code id}.
   *
   * @param id the identifier of the expense to delete
   * @throws DeleteExpenseException if no expense with the given ID exists
   */
  void deleteById(ExpenseId id);

  /**
   * Retrieves an expense, together with its shares and payers, by its unique identifier.
   *
   * @param id the identifier of the expense to retrieve
   * @return an {@link Optional} containing the {@link FullExpenseEntity} if found, otherwise
   *     empty
   */
  Optional<FullExpenseEntity> findById(ExpenseId id);

  /**
   * Retrieves a page of expenses associated with a specific group, ordered by completion time,
   * most recent first.
   *
   * @param groupId the identifier of the group for which to retrieve expenses
   * @param page the zero-based index of the page to retrieve
   * @param size the maximum number of expenses to include in the page
   * @return a page of {@link FullExpenseEntity} objects representing expenses associated with the
   *     specified group. If no expenses are found for the group, the returned page is empty.
   */
  Page<FullExpenseEntity> findAllByGroupId(GroupId groupId, int page, int size);
}
