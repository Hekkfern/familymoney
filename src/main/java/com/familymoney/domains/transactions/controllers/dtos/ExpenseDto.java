package com.familymoney.domains.transactions.controllers.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.javamoney.moneta.Money;

/**
 * Represents an expense transaction within a group.
 *
 * @param id the expense identifier
 * @param groupId the group identifier
 * @param description the expense description
 * @param totalAmount the total monetary amount of the expense
 * @param currency the ISO 4217 currency code of the expense
 * @param doneAt the time at which the expense was completed
 * @param createdBy the identifier of the user who created the expense
 * @param shares the amounts owed by each participating user
 * @param payers the amounts paid by each contributing user
 */
public record ExpenseDto(
    UUID id,
    UUID groupId,
    String description,
    Money totalAmount,
    String currency,
    Instant doneAt,
    UUID createdBy,
    List<ExpenseShareDto> shares,
    List<ExpensePayerDto> payers) {

  /**
   * Represents the portion of an expense owed by a user.
   *
   * @param userId the participating user identifier
   * @param amount the monetary amount owed by the user
   */
  public record ExpenseShareDto(UUID userId, Money amount) {}

  /**
   * Represents the portion of an expense paid by a user.
   *
   * @param userId the contributing user identifier
   * @param amount the monetary amount paid by the user
   */
  public record ExpensePayerDto(UUID userId, Money amount) {}
}
