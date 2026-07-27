package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.Balances;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class BalanceJooqMapper {

  private BalanceJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static BalanceEntity toEntity(final Record r) {
    return new BalanceEntity(
        BalanceId.fromString(String.valueOf(r.get(Balances.BALANCES.ID))),
        GroupId.fromString(String.valueOf(r.get(Balances.BALANCES.GROUP_ID))),
        Money.of(
            r.get(Balances.BALANCES.AMOUNT),
            Monetary.getCurrency(r.get(Balances.BALANCES.CURRENCY_CODE))),
        UserId.fromString(String.valueOf(r.get(Balances.BALANCES.USER_ID_1))),
        UserId.fromString(String.valueOf(r.get(Balances.BALANCES.USER_ID_2))));
  }
}
