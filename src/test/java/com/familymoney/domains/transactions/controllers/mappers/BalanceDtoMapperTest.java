package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.BalanceDto;
import com.familymoney.domains.transactions.services.data.BalanceData;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BalanceDtoMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final UserId creditor = UserId.generate();
      final UserId debitor = UserId.generate();
      final BigDecimal amount = new BigDecimal("123.456789");
      final BalanceData data = new BalanceData(creditor, debitor, Money.of(amount, "EUR"));

      final BalanceDto dto = BalanceDtoMapper.toDto(data);

      assertThat(dto.creditor()).isEqualTo(creditor.value());
      assertThat(dto.debitor()).isEqualTo(debitor.value());
      assertThat(dto.amount()).isEqualByComparingTo(amount);
      assertThat(dto.currency()).isEqualTo("EUR");
    }
  }
}
