package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupBalances;
import com.familymoney.generated.tables.Groups;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class BalanceJooqMapper {

  private BalanceJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static BalanceEntity toEntity(final Record r) {
    return new BalanceEntity(
        GroupId.fromUuid(r.get(GroupBalances.GROUP_BALANCES.GROUP_ID)),
        Money.of(
            r.get(GroupBalances.GROUP_BALANCES.AMOUNT),
            Monetary.getCurrency(r.get(Groups.GROUPS.CURRENCY_CODE))),
        UserId.fromUuid(r.get(GroupBalances.GROUP_BALANCES.USER_ID_1)),
        UserId.fromUuid(r.get(GroupBalances.GROUP_BALANCES.USER_ID_2)));
  }
}
