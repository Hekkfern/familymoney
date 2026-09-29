package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.exceptions.ExpenseNotFoundException;
import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.repositories.dtos.CreateExpenseDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateExpenseDto;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import java.util.Optional;
import org.springframework.data.domain.Page;

/**
 * Repository interface that defines persistence operations for expenses.
 *
 * <p>An expense records a total amount owed by a group, split across each member's {@code share} of
 * it, together with the amounts individually paid by each {@code payer} toward it. Amounts are
 * expressed in the currency of the group. The totals of shares and payers are guaranteed to match
 * by {@link CreateExpenseDto} and {@link UpdateExpenseDto}.
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
   * @throws GroupNotFoundException if the group of the expense does not exist
   * @throws UserNotFoundException if the creator, or any user in the shares or payers, does not
   *     exist
   */
  void create(CreateExpenseDto dto);

  /**
   * Updates the expense identified by {@code id} with the provided data. Only non-null fields of
   * {@code dto} are applied.
   *
   * @param id the identifier of the expense to update
   * @param dto the data containing the updated information for the expense. Non-null fields in the
   *     {@link UpdateExpenseDto} are used to update the corresponding fields of the expense.
   * @throws ExpenseNotFoundException if no expense with the given ID exists
   * @throws UserNotFoundException if any user in the updated shares or payers does not exist
   */
  void updateById(ExpenseId id, UpdateExpenseDto dto);

  /**
   * Deletes the expense identified by {@code id}.
   *
   * @param id the identifier of the expense to delete
   */
  void deleteById(ExpenseId id);

  /**
   * Retrieves an expense, together with its shares and payers, by its unique identifier.
   *
   * @param id the identifier of the expense to retrieve
   * @return an {@link Optional} containing the {@link FullExpenseEntity} if found, otherwise empty
   */
  Optional<FullExpenseEntity> findById(ExpenseId id);

  /**
   * Retrieves a page of expenses associated with a specific group, ordered by completion time, most
   * recent first.
   *
   * @param groupId the identifier of the group for which to retrieve expenses
   * @param page the zero-based index of the page to retrieve
   * @param size the maximum number of expenses to include in the page
   * @return a page of {@link FullExpenseEntity} objects representing expenses associated with the
   *     specified group. If no expenses are found for the group, the returned page is empty.
   */
  Page<FullExpenseEntity> findAllByGroupId(GroupId groupId, int page, int size);
}
