package com.familymoney.domains.transactions.controllers.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Describes a payment in the group timeline.
 *
 * @param id the payment identifier
 * @param amount the payment amount in the group currency
 * @param description the payment description
 * @param doneAt the completion time
 * @param fromUser the sender identifier
 * @param toUser the recipient identifier
 */
public record PaymentTransactionDto(
    UUID id, BigDecimal amount, String description, Instant doneAt, UUID fromUser, UUID toUser)
    implements TransactionDto {}
