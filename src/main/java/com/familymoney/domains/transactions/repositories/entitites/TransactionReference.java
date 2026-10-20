package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.services.data.TransactionType;
import java.time.Instant;
import java.util.UUID;

/**
 * Identifies an expense or payment in the transaction timeline.
 *
 * @param id the expense or payment ID
 * @param type the transaction type
 * @param doneAt the timestamp when the transaction was completed
 */
public record TransactionReference(UUID id, TransactionType type, Instant doneAt) {}
