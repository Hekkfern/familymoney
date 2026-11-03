package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseShareEntity;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.ExpenseSharesRecord;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpenseShareJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID userUuid = UUID.randomUUID();
      final BigDecimal amount = new BigDecimal("12.345");
      final ExpenseSharesRecord r = new ExpenseSharesRecord();
      r.setUserId(userUuid);
      r.setAmount(amount);

      final ExpenseShareEntity entity = ExpenseShareJooqMapper.toEntity(r);

      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.amount()).isEqualTo(amount);
    }
  }
}
