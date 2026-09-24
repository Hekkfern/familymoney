package com.familymoney.domains.transactions.controllers;

import static com.familymoney.utils.CustomHttp.IDEMPOTENCY_KEY_HEADER;

import com.familymoney.domains.idempotency.exceptions.IdempotencyConflictException;
import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
import com.familymoney.domains.transactions.exceptions.TransactionGroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.TransactionNotFoundException;
import com.familymoney.domains.transactions.exceptions.UserIsNotMemberOfGroupException;
import com.familymoney.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
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

/** Defines the HTTP API for managing payment transactions. */
@RequestMapping
public interface PaymentController {

  /**
   * Retrieves a page of payments recorded for a group, ordered by completion time, most recent
   * first. The authenticated user must be a member of the group.
   *
   * @param groupId the group identifier
   * @param page the zero-based index of the page to retrieve
   * @param size the maximum number of payments to include in the page, between 20 and 100
   * @return a page of payments for the group
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   */
  @Operation(summary = "Get the payments for a group")
  @GetMapping(path = "groups/{groupId}/payments", version = "1")
  PageResponse<PaymentDto> getPayments(
      @PathVariable @NotNull UUID groupId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);

  /**
   * Creates a payment in a group, settling part or all of the debt between its creditor and
   * debitor. The authenticated user must be a member of the group.
   *
   * @param idempotencyKey a client-supplied key that allows the request to be safely retried
   *     without creating a duplicate payment
   * @param groupId the group identifier
   * @param request the payment creation details
   * @throws TransactionGroupNotFoundException if no group with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     group
   * @throws IdempotencyConflictException if the idempotency key was already used with a
   *     different request body
   */
  @Operation(summary = "Create a payment in a group")
  @PostMapping(path = "groups/{groupId}/payments", version = "1")
  void createPayment(
      @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @PathVariable @NotNull UUID groupId,
      @RequestBody @Valid CreatePaymentRequestDto request,
      HttpServletRequest httpRequest);

  /**
   * Retrieves a single payment. The authenticated user must be a member of the payment's group.
   *
   * @param paymentId the payment identifier
   * @return the payment
   * @throws TransactionNotFoundException if no payment with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     payment's group
   */
  @Operation(summary = "Get a specific payment")
  @GetMapping(path = "payments/{paymentId}", version = "1")
  PaymentDto getPayment(@PathVariable @NotNull UUID paymentId);

  /**
   * Updates a payment. The authenticated user must be a member of the payment's group.
   *
   * @param paymentId the payment identifier
   * @param request the payment fields to update
   * @throws TransactionNotFoundException if no payment with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     payment's group
   */
  @Operation(summary = "Update a specific payment")
  @PatchMapping(path = "payments/{paymentId}", version = "1")
  void updatePayment(
      @PathVariable @NotNull UUID paymentId, @RequestBody @Valid UpdatePaymentRequestDto request);

  /**
   * Deletes a payment. The authenticated user must be a member of the payment's group.
   *
   * @param paymentId the payment identifier
   * @throws TransactionNotFoundException if no payment with the given ID exists
   * @throws UserIsNotMemberOfGroupException if the authenticated user is not a member of the
   *     payment's group
   */
  @Operation(summary = "Delete a payment")
  @DeleteMapping(path = "payments/{paymentId}", version = "1")
  void deletePayment(@PathVariable @NotNull UUID paymentId);
}
