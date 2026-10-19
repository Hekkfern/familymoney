package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.services.data.TransactionType;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionDtoMapperTest {

  @Nested
  class ToDto {

    private final ExpenseId id = ExpenseId.generate();
    private final TransactionData data =
        new TransactionData(
            id,
            Description.of("Groceries"),
            GroupId.generate(),
            Money.of(25, "EUR"),
            UserId.generate(),
            UserId.generate(),
            Instant.parse("2026-01-01T12:00:00Z"),
            Instant.parse("2026-01-01T13:00:00Z"));

    @Test
    void maps_successfully() {
      final TransactionDto dto = TransactionDtoMapper.toDto(data);

      assertThat(dto.type()).isEqualTo(TransactionType.EXPENSE);
      assertThat(dto.id()).isEqualTo(id.value());
    }
  }
}
