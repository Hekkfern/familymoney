package com.familymoney.domains.transactions.services.mappers;

import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.services.data.PaymentData;
import java.util.Currency;
import org.javamoney.moneta.Money;

public final class PaymentDataMapper {

  private PaymentDataMapper() {
    /* This utility class should not be instantiated */
  }

  public static PaymentData fromDbo(final PaymentEntity entity, final Currency currency) {
    return new PaymentData(
        entity.id(),
        entity.groupId(),
        entity.description(),
        Money.of(entity.amount(), currency.getCurrencyCode()),
        entity.doneAt(),
        entity.from(),
        entity.to());
  }
}
