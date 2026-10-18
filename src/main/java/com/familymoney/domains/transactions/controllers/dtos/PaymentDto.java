package com.familymoney.domains.transactions.controllers.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

/**
 * Represents a payment transaction within a group.
 *
 * @param id the payment identifier
 * @param groupId the group identifier
 * @param description the payment description
 * @param amount the paid monetary amount
 * @param currency the ISO 4217 currency code of the payment
 * @param doneAt the time at which the payment was completed
 * @param fromUserId the identifier of the user who made the payment
 * @param toUserId the identifier of the user who received the payment
 */
public record PaymentDto(
    UUID id,
    UUID groupId,
    String description,
    BigDecimal amount,
    Currency currency,
    Instant doneAt,
    UUID fromUserId,
    UUID toUserId) {}
