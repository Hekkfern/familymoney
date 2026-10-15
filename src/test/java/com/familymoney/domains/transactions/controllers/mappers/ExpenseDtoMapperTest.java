package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.ExpenseDto;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpenseDtoMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final ExpenseId id = ExpenseId.generate();
      final GroupId groupId = GroupId.generate();
      final UserId firstUser = UserId.generate();
      final UserId secondUser = UserId.generate();
      final BigDecimal firstShare = new BigDecimal("100.123456");
      final BigDecimal secondShare = new BigDecimal("23.333333");
      final BigDecimal totalAmount = firstShare.add(secondShare);
      final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
      final ExpenseData data =
          new ExpenseData(
              id,
              groupId,
              Description.of("Groceries"),
              Money.of(totalAmount, "GBP"),
              doneAt,
              Map.of(firstUser, firstShare, secondUser, secondShare),
              Map.of(secondUser, totalAmount));

      final ExpenseDto dto = ExpenseDtoMapper.toDto(data);

      assertThat(dto.id()).isEqualTo(id.value());
      assertThat(dto.groupId()).isEqualTo(groupId.value());
      assertThat(dto.description()).isEqualTo("Groceries");
      assertThat(dto.totalAmount()).isEqualByComparingTo(totalAmount);
      assertThat(dto.currency()).isEqualTo("GBP");
      assertThat(dto.doneAt()).isEqualTo(doneAt);
      assertThat(dto.shares())
          .containsExactlyInAnyOrder(
              new ExpenseDto.ExpenseShareDto(firstUser.value(), Money.of(firstShare, "GBP")),
              new ExpenseDto.ExpenseShareDto(secondUser.value(), Money.of(secondShare, "GBP")));
      assertThat(dto.payers())
          .containsExactly(
              new ExpenseDto.ExpensePayerDto(secondUser.value(), Money.of(totalAmount, "GBP")));
    }
  }
}
