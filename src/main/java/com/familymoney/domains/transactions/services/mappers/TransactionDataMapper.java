package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.TransactionEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;

public class TransactionDataMapper {

  private TransactionDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static TransactionData fromDbo(TransactionEntity entity) {
    return TransactionData.builder()
        .id(entity.id())
        .description(entity.description())
        .groupId(entity.groupId())
        .amount(entity.amount())
        .from(entity.from())
        .to(entity.to())
        .doneAt(entity.doneAt())
        .createdAt(entity.createdAt())
        .build();
  }
}
