package com.familymoney.domains.transactions.services.data;

/** Contains the complete expense or payment for a timeline entry. */
public sealed interface TransactionData {

  /**
   * Contains an expense with its shares and payers.
   *
   * @param expense the expense data
   */
  record Expense(ExpenseData expense) implements TransactionData {}

  /**
   * Contains a payment between group members.
   *
   * @param payment the payment data
   */
  record Payment(PaymentData payment) implements TransactionData {}
}
