package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateExpenseRequestMapperTest {

  @Nested
  class FromDto {

    @Test
    void maps_successfully() {
      final UserId firstUser = UserId.generate();
      final UserId secondUser = UserId.generate();
      final BigDecimal firstShare = new BigDecimal("10.123456");
      final BigDecimal secondShare = new BigDecimal("20.654321");
      final BigDecimal totalAmount = firstShare.add(secondShare);
      final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto(
              "Groceries",
              doneAt,
              List.of(
                  new UpdateExpenseRequestDto.ExpenseShareDto(firstUser.value(), firstShare),
                  new UpdateExpenseRequestDto.ExpenseShareDto(secondUser.value(), secondShare)),
              List.of(
                  new UpdateExpenseRequestDto.ExpensePayerDto(secondUser.value(), totalAmount)));

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isEqualTo(Description.of("Groceries"));
      assertThat(data.doneAt()).isEqualTo(doneAt);
      assertThat(data.shares()).isEqualTo(Map.of(firstUser, firstShare, secondUser, secondShare));
      assertThat(data.payers()).isEqualTo(Map.of(secondUser, totalAmount));
    }

    @Test
    void preserves_omitted_fields_in_partial_update() {
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto("Groceries", null, null, null);

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isEqualTo(Description.of("Groceries"));
      assertThat(data.doneAt()).isNull();
      assertThat(data.shares()).isNull();
      assertThat(data.payers()).isNull();
    }

    @Test
    void preserves_all_omitted_fields() {
      final UpdateExpenseRequestDto dto = new UpdateExpenseRequestDto(null, null, null, null);

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isNull();
      assertThat(data.doneAt()).isNull();
      assertThat(data.shares()).isNull();
      assertThat(data.payers()).isNull();
      assertThat(data.isEmpty()).isTrue();
    }
  }
}
