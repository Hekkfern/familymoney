package com.familymoney.domains.transactions.controllers;

import static com.familymoney.utils.CustomHttp.IDEMPOTENCY_KEY_HEADER;

import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Defines the HTTP API for managing expense transactions. */
@RequestMapping
public interface ExpenseController {

  /**
   * Retrieves expenses for a group.
   *
   * @param groupId the group identifier
   * @param page the zero-based page index
   * @param size the maximum number of expenses to return
   * @return a page of expenses
   */
  @Operation(summary = "Get the expenses for a group")
  @GetMapping(path = "groups/{groupId}/expenses", version = "1")
  PageResponse<ExpenseDto> getExpenses(
      @PathVariable @NotNull UUID groupId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);

  /**
   * Creates an expense in a group.
   *
   * @param groupId the group identifier
   * @param request the expense creation details
   */
  @Operation(summary = "Create an expense in a group")
  @PostMapping(path = "groups/{groupId}/expenses", version = "1")
  void createExpense(
      @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @PathVariable @NotNull UUID groupId,
      @RequestBody @Valid CreateExpenseRequestDto request);

  /**
   * Retrieves an expense.
   *
   * @param expenseId the expense identifier
   * @return the expense
   */
  @Operation(summary = "Get a specific expense")
  @GetMapping(path = "expenses/{expenseId}", version = "1")
  ExpenseDto getExpense(@PathVariable @NotNull UUID expenseId);

  /**
   * Updates an expense.
   *
   * @param expenseId the expense identifier
   * @param request the expense updates
   */
  @Operation(summary = "Update a specific expense")
  @PatchMapping(path = "expenses/{expenseId}", version = "1")
  void updateExpense(
      @PathVariable @NotNull UUID expenseId, @RequestBody @Valid UpdateExpenseRequestDto request);

  /**
   * Deletes an expense.
   *
   * @param expenseId the expense identifier
   */
  @Operation(summary = "Delete an expense")
  @DeleteMapping(path = "expenses/{expenseId}", version = "1")
  void deleteExpense(@PathVariable @NotNull UUID expenseId);
}
