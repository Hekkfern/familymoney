package com.familymoney.domains.transactions.controllers.dtos;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Describes an expense in the group timeline.
 *
 * @param id the expense identifier
 * @param amount the sum of the expense shares in the group currency
 * @param description the expense description
 * @param doneAt the completion time
 * @param payers the sorted identifiers of users who paid
 * @param shares the sorted identifiers of users who share the expense
 */
public record ExpenseTransactionDto(
    UUID id,
    BigDecimal amount,
    String description,
    Instant doneAt,
    List<UUID> payers,
    List<UUID> shares)
    implements TransactionDto {}
