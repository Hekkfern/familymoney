package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupBalances;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class BalanceJooqMapper {

  private BalanceJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static BalanceEntity toEntity(final Record record) {
    return new BalanceEntity(
        BalanceId.fromUuid(record.get(GroupBalances.GROUP_BALANCES.ID)),
        GroupId.fromUuid(record.get(GroupBalances.GROUP_BALANCES.GROUP_ID)),
        Money.of(
            record.get(GroupBalances.GROUP_BALANCES.AMOUNT),
            Monetary.getCurrency(record.get(GroupBalances.GROUP_BALANCES.CURRENCY_CODE))),
        UserId.fromUuid(record.get(GroupBalances.GROUP_BALANCES.USER_ID_1)),
        UserId.fromUuid(record.get(GroupBalances.GROUP_BALANCES.USER_ID_2)));
  }
}
