package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
import com.familymoney.domains.transactions.services.data.UpdatePaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdatePaymentRequestMapperTest {

  @Nested
  class FromDto {

    @Test
    void maps_successfully() {
      final UserId from = UserId.generate();
      final UserId to = UserId.generate();
      final BigDecimal amount = new BigDecimal("123.456");
      final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
      final UpdatePaymentRequestDto dto =
          new UpdatePaymentRequestDto("Settlement", from.value(), to.value(), amount, doneAt);

      final UpdatePaymentData data = UpdatePaymentRequestMapper.fromDto(dto);

      assertThat(data.description()).isEqualTo(Description.of("Settlement"));
      assertThat(data.doneAt()).isEqualTo(doneAt);
      assertThat(data.amount()).isEqualTo(amount);
      assertThat(data.from()).isEqualTo(from);
      assertThat(data.to()).isEqualTo(to);
      assertThat(data.isEmpty()).isFalse();
    }

    @Test
    void preserves_omitted_fields_in_partial_update() {
      final UpdatePaymentRequestDto dto =
          new UpdatePaymentRequestDto("Settlement", null, null, null, null);

      final UpdatePaymentData data = UpdatePaymentRequestMapper.fromDto(dto);

      assertThat(data.description()).isEqualTo(Description.of("Settlement"));
      assertThat(data.doneAt()).isNull();
      assertThat(data.amount()).isNull();
      assertThat(data.from()).isNull();
      assertThat(data.to()).isNull();
      assertThat(data.isEmpty()).isFalse();
    }

    @Test
    void preserves_all_omitted_fields() {
      final UpdatePaymentRequestDto dto = new UpdatePaymentRequestDto(null, null, null, null, null);

      final UpdatePaymentData data = UpdatePaymentRequestMapper.fromDto(dto);

      assertThat(data.description()).isNull();
      assertThat(data.doneAt()).isNull();
      assertThat(data.amount()).isNull();
      assertThat(data.from()).isNull();
      assertThat(data.to()).isNull();
      assertThat(data.isEmpty()).isTrue();
    }

    @Test
    void preserves_amount_only_update() {
      final BigDecimal amount = new BigDecimal("123.456");
      final UpdatePaymentRequestDto dto =
          new UpdatePaymentRequestDto(null, null, null, amount, null);

      final UpdatePaymentData data = UpdatePaymentRequestMapper.fromDto(dto);

      assertThat(data.description()).isNull();
      assertThat(data.doneAt()).isNull();
      assertThat(data.amount()).isEqualTo(amount);
      assertThat(data.from()).isNull();
      assertThat(data.to()).isNull();
      assertThat(data.isEmpty()).isFalse();
    }
  }
}
