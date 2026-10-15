package com.familymoney.domains.transactions.controllers.dtos;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents an outstanding balance between two group members.
 *
 * @param creditor the user who is owed money
 * @param debitor the user who owes money
 * @param amount the outstanding monetary amount
 * @param currency the ISO 4217 currency code of the balance
 */
public record BalanceDto(UUID creditor, UUID debitor, BigDecimal amount, String currency) {}
