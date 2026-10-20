package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;
import java.util.Currency;

/** Maps persisted expenses and payments to the transaction variants. */
public final class TransactionDataMapper {

  private TransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  /**
   * Maps an expense and its participants.
   *
   * @param entity the expense with its shares and payers
   * @param currency the group currency
   * @return the expense transaction
   */
  public static TransactionData.Expense fromExpense(
      final FullExpenseEntity entity, final Currency currency) {
    return new TransactionData.Expense(ExpenseDataMapper.fromDbo(entity, currency));
  }

  /**
   * Maps a payment.
   *
   * @param entity the payment
   * @param currency the group currency
   * @return the payment transaction
   */
  public static TransactionData.Payment fromPayment(
      final PaymentEntity entity, final Currency currency) {
    return new TransactionData.Payment(PaymentDataMapper.fromDbo(entity, currency));
  }
}
