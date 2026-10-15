package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentDtoMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final PaymentId id = PaymentId.generate();
      final GroupId groupId = GroupId.generate();
      final UserId from = UserId.generate();
      final UserId to = UserId.generate();
      final Money amount = Money.of(new BigDecimal("123.456"), "GBP");
      final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
      final PaymentData data =
          new PaymentData(id, groupId, Description.of("Settlement"), amount, doneAt, from, to);

      final PaymentDto dto = PaymentDtoMapper.toDto(data);

      assertThat(dto.id()).isEqualTo(id.value());
      assertThat(dto.groupId()).isEqualTo(groupId.value());
      assertThat(dto.description()).isEqualTo("Settlement");
      assertThat(dto.amount()).isEqualTo(amount);
      assertThat(dto.currency()).isEqualTo("GBP");
      assertThat(dto.doneAt()).isEqualTo(doneAt);
      assertThat(dto.fromUserId()).isEqualTo(from.value());
      assertThat(dto.toUserId()).isEqualTo(to.value());
    }
  }
}
