package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;

public final class TransactionDataMapper {

  private TransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static TransactionData fromDbo(TransactionEntity entity) {
    return new TransactionData(
        entity.id(),
        entity.description(),
        entity.groupId(),
        entity.amount(),
        entity.from(),
        entity.to(),
        entity.doneAt(),
        entity.createdAt());
  }
}
