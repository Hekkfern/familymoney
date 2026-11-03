package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.ExpensePaymentEntity;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.ExpensePaymentsRecord;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpensePaymentJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID userUuid = UUID.randomUUID();
      final BigDecimal amount = new BigDecimal("12.345");
      final ExpensePaymentsRecord r = new ExpensePaymentsRecord();
      r.setUserId(userUuid);
      r.setAmount(amount);

      final ExpensePaymentEntity entity = ExpensePaymentJooqMapper.toEntity(r);

      assertThat(entity.userId()).isEqualTo(UserId.fromUuid(userUuid));
      assertThat(entity.amount()).isEqualTo(amount);
    }
  }
}
