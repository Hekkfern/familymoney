package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.services.data.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Defines the common fields of expense and payment transactions. */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PaymentTransactionDto.class, name = "PAYMENT"),
  @JsonSubTypes.Type(value = ExpenseTransactionDto.class, name = "EXPENSE")
})
public sealed interface TransactionDto permits PaymentTransactionDto, ExpenseTransactionDto {

  /**
   * Returns the transaction type.
   *
   * @return the expense or payment type
   */
  @JsonProperty("type")
  default TransactionType type() {
    return switch (this) {
      case ExpenseTransactionDto _ -> TransactionType.EXPENSE;
      case PaymentTransactionDto _ -> TransactionType.PAYMENT;
    };
  }

  /**
   * Returns the expense or payment identifier.
   *
   * @return the transaction identifier
   */
  UUID id();

  /**
   * Returns the amount in the group currency.
   *
   * @return the total expense amount or payment amount
   */
  BigDecimal amount();

  /**
   * Returns the transaction description.
   *
   * @return the description
   */
  String description();

  /**
   * Returns the completion time from the transaction.
   *
   * @return the completion time
   */
  Instant doneAt();
}
