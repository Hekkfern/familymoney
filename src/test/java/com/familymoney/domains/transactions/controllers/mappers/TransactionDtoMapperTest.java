package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.ExpenseTransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentTransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionDtoMapperTest {

  @Nested
  class ToDto {

    private final Instant doneAt = Instant.parse("2026-01-01T12:00:00Z");
    private final UserId lowUser =
        UserId.fromUuid(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private final UserId highUser =
        UserId.fromUuid(UUID.fromString("00000000-0000-0000-0000-000000000002"));

    @Test
    void maps_all_expense_fields_and_sorts_participant_ids() {
      final ExpenseId id = ExpenseId.generate();
      final Map<UserId, BigDecimal> participants =
          Map.of(highUser, new BigDecimal("12.345"), lowUser, new BigDecimal("7.655"));
      final ExpenseData expense =
          new ExpenseData(
              id,
              GroupId.generate(),
              Description.of("Groceries"),
              Money.of(new BigDecimal("20.000"), "EUR"),
              doneAt,
              participants,
              participants);

      final TransactionDto result =
          TransactionDtoMapper.toDto(new TransactionData.Expense(expense));

      assertThat(result).isInstanceOf(ExpenseTransactionDto.class);
      final ExpenseTransactionDto dto = (ExpenseTransactionDto) result;
      assertThat(dto.id()).isEqualTo(id.value());
      assertThat(dto.amount()).isEqualByComparingTo("20.000");
      assertThat(dto.description()).isEqualTo("Groceries");
      assertThat(dto.doneAt()).isEqualTo(doneAt);
      assertThat(dto.payers()).containsExactly(lowUser.value(), highUser.value());
      assertThat(dto.shares()).containsExactly(lowUser.value(), highUser.value());
    }

    @Test
    void maps_all_payment_fields() {
      final PaymentId id = PaymentId.generate();
      final PaymentData payment =
          new PaymentData(
              id,
              GroupId.generate(),
              Description.of("Settlement"),
              Money.of(new BigDecimal("7.125"), "EUR"),
              doneAt,
              highUser,
              lowUser);

      final TransactionDto result =
          TransactionDtoMapper.toDto(new TransactionData.Payment(payment));

      assertThat(result).isInstanceOf(PaymentTransactionDto.class);
      final PaymentTransactionDto dto = (PaymentTransactionDto) result;
      assertThat(dto.id()).isEqualTo(id.value());
      assertThat(dto.amount()).isEqualByComparingTo("7.125");
      assertThat(dto.description()).isEqualTo("Settlement");
      assertThat(dto.doneAt()).isEqualTo(doneAt);
      assertThat(dto.fromUser()).isEqualTo(highUser.value());
      assertThat(dto.toUser()).isEqualTo(lowUser.value());
    }
  }
}
