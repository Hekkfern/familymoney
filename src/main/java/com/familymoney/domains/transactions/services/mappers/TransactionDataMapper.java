package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.services.data.TransactionType;

public final class TransactionDataMapper {

  private TransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static TransactionData fromExpense(ExpenseEntity entity) {
    return new TransactionData(entity.id().value(), TransactionType.EXPENSE);
  }

  public static TransactionData fromPayment(PaymentEntity entity) {
    return new TransactionData(entity.id().value(), TransactionType.PAYMENT);
  }
}
