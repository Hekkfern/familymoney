package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.records.PaymentsRecord;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID paymentUuid = UUID.randomUUID();
      final String description = "Settle up";
      final BigDecimal amount = new BigDecimal("12.345");
      final UUID fromUuid = UUID.randomUUID();
      final UUID toUuid = UUID.randomUUID();
      final UUID groupUuid = UUID.randomUUID();
      final OffsetDateTime doneAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final PaymentsRecord r = new PaymentsRecord();
      r.setId(paymentUuid);
      r.setDescription(description);
      r.setAmount(amount);
      r.setFromUser(fromUuid);
      r.setToUser(toUuid);
      r.setGroupId(groupUuid);
      r.setDoneAt(doneAt);

      final PaymentEntity entity = PaymentJooqMapper.toEntity(r);

      assertThat(entity.id()).isEqualTo(PaymentId.fromUuid(paymentUuid));
      assertThat(entity.description()).isEqualTo(Description.of(description));
      assertThat(entity.amount()).isEqualTo(amount);
      assertThat(entity.from()).isEqualTo(UserId.fromUuid(fromUuid));
      assertThat(entity.to()).isEqualTo(UserId.fromUuid(toUuid));
      assertThat(entity.groupId()).isEqualTo(GroupId.fromUuid(groupUuid));
      assertThat(entity.doneAt()).isEqualTo(doneAt.toInstant());
    }
  }
}
