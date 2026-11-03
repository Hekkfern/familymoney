package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupBalances;
import com.familymoney.generated.tables.Groups;
import java.math.BigDecimal;
import java.util.UUID;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BalanceJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID groupUuid = UUID.randomUUID();
      final UUID user1Uuid = UUID.randomUUID();
      final UUID user2Uuid = UUID.randomUUID();
      final BigDecimal amount = new BigDecimal("12.345");
      final String currencyCode = "EUR";
      final Record r =
          DSL.using(SQLDialect.POSTGRES)
              .newRecord(
                  GroupBalances.GROUP_BALANCES.GROUP_ID,
                  GroupBalances.GROUP_BALANCES.AMOUNT,
                  GroupBalances.GROUP_BALANCES.USER_ID_1,
                  GroupBalances.GROUP_BALANCES.USER_ID_2,
                  Groups.GROUPS.CURRENCY_CODE);
      r.set(GroupBalances.GROUP_BALANCES.GROUP_ID, groupUuid);
      r.set(GroupBalances.GROUP_BALANCES.AMOUNT, amount);
      r.set(GroupBalances.GROUP_BALANCES.USER_ID_1, user1Uuid);
      r.set(GroupBalances.GROUP_BALANCES.USER_ID_2, user2Uuid);
      r.set(Groups.GROUPS.CURRENCY_CODE, currencyCode);

      final BalanceEntity entity = BalanceJooqMapper.toEntity(r);

      assertThat(entity.groupId()).isEqualTo(GroupId.fromUuid(groupUuid));
      assertThat(entity.money()).isEqualTo(Money.of(amount, Monetary.getCurrency(currencyCode)));
      assertThat(entity.user1()).isEqualTo(UserId.fromUuid(user1Uuid));
      assertThat(entity.user2()).isEqualTo(UserId.fromUuid(user2Uuid));
    }
  }
}
