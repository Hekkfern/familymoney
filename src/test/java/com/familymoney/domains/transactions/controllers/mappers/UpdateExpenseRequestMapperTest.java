package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.controllers.dtos.UpdateExpenseRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.testutils.FakeGenerator;
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
      final String description = FakeGenerator.description();
      final UserId firstUser = UserId.generate();
      final UserId secondUser = UserId.generate();
      final BigDecimal firstShare = new BigDecimal("10.123456");
      final BigDecimal secondShare = new BigDecimal("20.654321");
      final BigDecimal totalAmount = firstShare.add(secondShare);
      final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto(
              description,
              doneAt,
              List.of(
                  new UpdateExpenseRequestDto.ExpenseShareDto(firstUser.value(), firstShare),
                  new UpdateExpenseRequestDto.ExpenseShareDto(secondUser.value(), secondShare)),
              List.of(
                  new UpdateExpenseRequestDto.ExpensePayerDto(secondUser.value(), totalAmount)));

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isEqualTo(Description.of(description));
      assertThat(data.doneAt()).isEqualTo(doneAt);
      assertThat(data.shares()).isEqualTo(Map.of(firstUser, firstShare, secondUser, secondShare));
      assertThat(data.payers()).isEqualTo(Map.of(secondUser, totalAmount));
    }

    @Test
    void preserves_description_only_update() {
      final String description = FakeGenerator.description();
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto(description, null, null, null);

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isEqualTo(Description.of(description));
      assertThat(data.doneAt()).isNull();
      assertThat(data.shares()).isNull();
      assertThat(data.payers()).isNull();
    }

    @Test
    void preserves_done_at_only_update() {
      final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
      final UpdateExpenseRequestDto dto = new UpdateExpenseRequestDto(null, doneAt, null, null);

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isNull();
      assertThat(data.doneAt()).isEqualTo(doneAt);
      assertThat(data.shares()).isNull();
      assertThat(data.payers()).isNull();
    }

    @Test
    void throws_when_no_field_is_provided() {
      final UpdateExpenseRequestDto dto = new UpdateExpenseRequestDto(null, null, null, null);

      assertThatThrownBy(() -> UpdateExpenseRequestMapper.fromDto(dto))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void maps_shares_and_payers_without_other_fields() {
      final UserId user = UserId.generate();
      final BigDecimal amount = new BigDecimal("15.50");
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto(
              null,
              null,
              List.of(new UpdateExpenseRequestDto.ExpenseShareDto(user.value(), amount)),
              List.of(new UpdateExpenseRequestDto.ExpensePayerDto(user.value(), amount)));

      final UpdateExpenseData data = UpdateExpenseRequestMapper.fromDto(dto);

      assertThat(data.description()).isNull();
      assertThat(data.doneAt()).isNull();
      assertThat(data.shares()).isEqualTo(Map.of(user, amount));
      assertThat(data.payers()).isEqualTo(Map.of(user, amount));
    }

    @Test
    void throws_when_a_user_appears_twice_in_shares() {
      final UserId user = UserId.generate();
      final BigDecimal amount = new BigDecimal("10");
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto(
              null,
              null,
              List.of(
                  new UpdateExpenseRequestDto.ExpenseShareDto(user.value(), amount),
                  new UpdateExpenseRequestDto.ExpenseShareDto(user.value(), amount)),
              List.of(new UpdateExpenseRequestDto.ExpensePayerDto(user.value(), amount)));

      assertThatThrownBy(() -> UpdateExpenseRequestMapper.fromDto(dto))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void throws_when_a_user_appears_twice_in_payers() {
      final UserId user = UserId.generate();
      final BigDecimal amount = new BigDecimal("10");
      final UpdateExpenseRequestDto dto =
          new UpdateExpenseRequestDto(
              null,
              null,
              List.of(new UpdateExpenseRequestDto.ExpenseShareDto(user.value(), amount)),
              List.of(
                  new UpdateExpenseRequestDto.ExpensePayerDto(user.value(), amount),
                  new UpdateExpenseRequestDto.ExpensePayerDto(user.value(), amount)));

      assertThatThrownBy(() -> UpdateExpenseRequestMapper.fromDto(dto))
          .isInstanceOf(IllegalStateException.class);
    }
  }
}
