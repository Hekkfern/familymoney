package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.Balances;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

public final class BalanceJooqMapper {

  private BalanceJooqMapper() {}

  public static BalanceEntity toEntity(final Record r) {
    return BalanceEntity.builder()
        .id(BalanceId.fromString(String.valueOf(r.get(Balances.BALANCES.ID))))
        .groupId(GroupId.fromString(String.valueOf(r.get(Balances.BALANCES.GROUP_ID))))
        .money(
            Money.of(
                r.get(Balances.BALANCES.AMOUNT),
                Monetary.getCurrency(r.get(Balances.BALANCES.CURRENCY_CODE))))
        .user1(UserId.fromString(String.valueOf(r.get(Balances.BALANCES.USER_ID_1))))
        .user2(UserId.fromString(String.valueOf(r.get(Balances.BALANCES.USER_ID_2))))
        .build();
  }
}
