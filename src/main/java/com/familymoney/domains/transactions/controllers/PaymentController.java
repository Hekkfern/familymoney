package com.familymoney.domains.transactions.controllers;

import static com.familymoney.utils.CustomHttp.IDEMPOTENCY_KEY_HEADER;

import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
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

/** Defines the HTTP API for managing payment transactions. */
@RequestMapping
public interface PaymentController {

  /**
   * Retrieves payments for a group.
   *
   * @param groupId the group identifier
   * @param page the zero-based page index
   * @param size the maximum number of payments to return
   * @return a page of payments
   */
  @Operation(summary = "Get the payments for a group")
  @GetMapping(path = "groups/{groupId}/payments", version = "1")
  PageResponse<PaymentDto> getPayments(
      @PathVariable @NotNull UUID groupId,
      @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
      @RequestParam(defaultValue = "20") @Min(20) @Max(100) int size);

  /**
   * Creates a payment in a group.
   *
   * @param groupId the group identifier
   * @param request the payment creation details
   */
  @Operation(summary = "Create a payment in a group")
  @PostMapping(path = "groups/{groupId}/payments", version = "1")
  void createPayment(
      @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
      @PathVariable @NotNull UUID groupId,
      @RequestBody @Valid CreatePaymentRequestDto request);

  /**
   * Retrieves a payment.
   *
   * @param paymentId the payment identifier
   * @return the payment
   */
  @Operation(summary = "Get a specific payment")
  @GetMapping(path = "payments/{paymentId}", version = "1")
  PaymentDto getPayment(@PathVariable @NotNull UUID paymentId);

  /**
   * Updates a payment.
   *
   * @param paymentId the payment identifier
   * @param request the payment updates
   */
  @Operation(summary = "Update a specific payment")
  @PatchMapping(path = "payments/{paymentId}", version = "1")
  void updatePayment(
      @PathVariable @NotNull UUID paymentId, @RequestBody @Valid UpdatePaymentRequestDto request);

  /**
   * Deletes a payment.
   *
   * @param paymentId the payment identifier
   */
  @Operation(summary = "Delete a payment")
  @DeleteMapping(path = "payments/{paymentId}", version = "1")
  void deletePayment(@PathVariable @NotNull UUID paymentId);
}
