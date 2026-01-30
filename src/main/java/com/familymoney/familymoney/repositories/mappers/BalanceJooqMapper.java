package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.generated.tables.Balances;
import com.familymoney.familymoney.repositories.dbos.BalanceDbo;
import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class BalanceJooqMapper {

  private BalanceJooqMapper() {}

  public static BalanceDbo toDbo(final Record r) {
    return BalanceDbo.builder()
        .id(BalanceId.fromString(String.valueOf(r.get(Balances.BALANCES.ID))))
        .groupId(GroupId.fromString(String.valueOf(r.get(Balances.BALANCES.GROUP_ID))))
        .amount(
            Money.of(
                r.get(Balances.BALANCES.AMOUNT),
                Monetary.getCurrency(r.get(Balances.BALANCES.CURRENCY_CODE))))
        .user1(UserId.fromString(String.valueOf(r.get(Balances.BALANCES.USER_ID_1))))
        .user2(UserId.fromString(String.valueOf(r.get(Balances.BALANCES.USER_ID_2))))
        .build();
  }
}
