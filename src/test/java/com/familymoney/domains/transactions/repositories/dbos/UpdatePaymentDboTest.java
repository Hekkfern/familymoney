package com.familymoney.domains.transactions.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.testutils.FakeGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdatePaymentDboTest {

  private static final Instant INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> new UpdatePaymentDbo(null, null, null, null, null));
    }

    @Test
    void accepts_update_with_only_description() {
      final Description description = Description.of(FakeGenerator.description());

      final UpdatePaymentDbo dbo = UpdatePaymentDbo.builder().description(description).build();

      assertThat(dbo.description()).isEqualTo(description);
    }

    @Test
    void accepts_update_with_only_done_at() {
      final UpdatePaymentDbo dbo = UpdatePaymentDbo.builder().doneAt(INSTANT).build();

      assertThat(dbo.doneAt()).isEqualTo(INSTANT);
    }

    @Test
    void accepts_update_with_only_from_user() {
      final UserId from = UserId.generate();

      final UpdatePaymentDbo dbo = UpdatePaymentDbo.builder().from(from).build();

      assertThat(dbo.from()).isEqualTo(from);
    }

    @Test
    void accepts_update_with_only_to_user() {
      final UserId to = UserId.generate();

      final UpdatePaymentDbo dbo = UpdatePaymentDbo.builder().to(to).build();

      assertThat(dbo.to()).isEqualTo(to);
    }

    @Test
    void accepts_update_with_different_from_and_to_users() {
      final UpdatePaymentDbo dbo =
          UpdatePaymentDbo.builder().from(UserId.generate()).to(UserId.generate()).build();

      assertThat(dbo.from()).isNotEqualTo(dbo.to());
    }

    @Test
    void rejects_update_with_same_from_and_to_users() {
      final UserId user = UserId.generate();

      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdatePaymentDbo.builder().from(user).to(user).build());
    }

    @Test
    void accepts_amount_with_three_decimals() {
      final BigDecimal amount = new BigDecimal("10.123");

      final UpdatePaymentDbo dbo = UpdatePaymentDbo.builder().amount(amount).build();

      assertThat(dbo.amount()).isEqualTo(amount);
    }

    @Test
    void rejects_zero_amount() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdatePaymentDbo.builder().amount(BigDecimal.ZERO).build());
    }

    @Test
    void rejects_negative_amount() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdatePaymentDbo.builder().amount(new BigDecimal("-1")).build());
    }

    @Test
    void rejects_amount_with_more_than_three_decimals() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdatePaymentDbo.builder().amount(new BigDecimal("1.0001")).build());
    }
  }
}
