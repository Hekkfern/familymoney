package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.UpdateTransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.Map;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

  /**
   * Get the balance between all users in a group
   *
   * @param groupId Identifier of the group
   * @param userId Identifier of the user requesting the balances
   * @return Map of user identifiers to their respective balances
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  Map<UserId, Money> getAllGroupBalances(GroupId groupId, UserId userId);

  /**
   * Get the balance between all users in a group as an administrator
   *
   * @param groupId Identifier of the group
   * @return Map of user identifiers to their respective balances
   * @throws TransactionGroupNotFoundException if the group does not exist
   */
  Map<UserId, Money> getAllGroupBalancesAsAdmin(GroupId groupId);

  /**
   * Get a paginated list of individual expenses in a group, ordered by most recent first
   *
   * @param groupId Identifier of the group
   * @param userId Identifier of the user requesting the balances
   * @param pageable Pagination information
   * @return Paginated list of expenses
   * @throws TransactionGroupNotFoundException if the group does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  Page<TransactionData> getGroupTransactions(GroupId groupId, UserId userId, Pageable pageable);

  /**
   * Get a paginated list of individual expenses in a group as an administrator, ordered by most
   * recent first
   *
   * @param groupId Identifier of the group
   * @param pageable Pagination information
   * @return Paginated list of expenses
   * @throws TransactionGroupNotFoundException if the group does not exist
   */
  Page<TransactionData> getGroupTransactionsAsAdmin(GroupId groupId, Pageable pageable);

  /**
   * Create a new transaction in a group
   *
   * @param groupId Identifier of the group
   * @param description Description of the transaction
   * @param from Identifier of the user sending the money
   * @param to Identifier of the user receiving the money
   * @param amount Amount of money
   * @param doneAt Timestamp when the transaction was done
   * @param createdBy Identifier of the user creating the transaction
   */
  void createTransactionInGroup(
      GroupId groupId,
      Description description,
      UserId from,
      UserId to,
      Money amount,
      Instant doneAt,
      UserId createdBy);

  /**
   * Create a new transaction in a group as an administrator.
   *
   * @param groupId Identifier of the group
   * @param description Description of the transaction
   * @param from Identifier of the user sending the money
   * @param to Identifier of the user receiving the money
   * @param amount Amount of money
   * @param doneAt Timestamp when the transaction was done
   */
  void createTransactionInGroupAsAdmin(
      GroupId groupId,
      Description description,
      UserId from,
      UserId to,
      Money amount,
      Instant doneAt);

  /**
   * Update one or more fields of a transaction. Only non-null fields in the data parameter will be
   * updated.
   *
   * @param userId Identifier of the user requesting the update
   * @param expenseId Identifier of the transaction to update
   * @param data Data to update. Only non-null fields will be updated
   * @throws TransactionNotFoundException if the transaction does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void updateTransaction(UserId userId, ExpenseId expenseId, UpdateTransactionData data);

  /**
   * Update one or more fields of a transaction as an administrator. Only non-null fields in the
   * data parameter will be updated.
   *
   * @param expenseId Identifier of the transaction to update
   * @param data Data to update. Only non-null fields will be updated
   * @throws TransactionNotFoundException if the transaction does not exist
   */
  void updateTransactionAsAdmin(ExpenseId expenseId, UpdateTransactionData data);

  /**
   * Delete a transaction
   *
   * @param userId Identifier of the user requesting the deletion
   * @param expenseId Identifier of the transaction to delete
   * @throws TransactionNotFoundException if the transaction does not exist
   * @throws UserIsNotMemberOfGroupException if the user is not a member of the group
   */
  void deleteTransaction(UserId userId, ExpenseId expenseId);

  /**
   * Delete a transaction as an administrator.
   *
   * @param expenseId Identifier of the transaction to delete
   * @throws TransactionNotFoundException if the transaction does not exist
   */
  void deleteTransactionAsAdmin(ExpenseId expenseId);
}
