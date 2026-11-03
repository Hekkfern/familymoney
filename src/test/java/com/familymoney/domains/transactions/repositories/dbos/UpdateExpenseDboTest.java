package com.familymoney.domains.transactions.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.testutils.FakeGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateExpenseDboTest {

  private static final Instant INSTANT = Instant.parse("2026-01-01T00:00:00Z");

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> new UpdateExpenseDbo(null, null, null, null));
    }

    @Test
    void accepts_update_with_only_description() {
      final Description description = Description.of(FakeGenerator.description());

      final UpdateExpenseDbo dbo = UpdateExpenseDbo.builder().description(description).build();

      assertThat(dbo.description()).isEqualTo(description);
    }

    @Test
    void accepts_update_with_only_done_at() {
      final UpdateExpenseDbo dbo = UpdateExpenseDbo.builder().doneAt(INSTANT).build();

      assertThat(dbo.doneAt()).isEqualTo(INSTANT);
    }

    @Test
    void rejects_shares_without_payers() {
      final Map<UserId, BigDecimal> shares = Map.of(UserId.generate(), BigDecimal.TEN);

      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdateExpenseDbo.builder().shares(shares).build());
    }

    @Test
    void rejects_payers_without_shares() {
      final Map<UserId, BigDecimal> payers = Map.of(UserId.generate(), BigDecimal.TEN);

      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdateExpenseDbo.builder().payers(payers).build());
    }

    @Test
    void rejects_empty_shares_and_payers() {
      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdateExpenseDbo.builder().shares(Map.of()).payers(Map.of()).build());
    }

    @Test
    void rejects_zero_amounts() {
      final Map<UserId, BigDecimal> amounts = Map.of(UserId.generate(), BigDecimal.ZERO);

      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdateExpenseDbo.builder().shares(amounts).payers(amounts).build());
    }

    @Test
    void rejects_negative_amounts() {
      final Map<UserId, BigDecimal> amounts = Map.of(UserId.generate(), new BigDecimal("-1"));

      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdateExpenseDbo.builder().shares(amounts).payers(amounts).build());
    }

    @Test
    void rejects_amounts_with_more_than_three_decimals() {
      final Map<UserId, BigDecimal> amounts = Map.of(UserId.generate(), new BigDecimal("1.0001"));

      assertThatIllegalArgumentException()
          .isThrownBy(() -> UpdateExpenseDbo.builder().shares(amounts).payers(amounts).build());
    }

    @Test
    void rejects_shares_and_payers_with_different_totals() {
      final Map<UserId, BigDecimal> shares = Map.of(UserId.generate(), BigDecimal.TEN);
      final Map<UserId, BigDecimal> payers = Map.of(UserId.generate(), BigDecimal.ONE);

      assertThatThrownBy(() -> UpdateExpenseDbo.builder().shares(shares).payers(payers).build())
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_shares_and_payers_with_matching_totals() {
      final Map<UserId, BigDecimal> shares =
          Map.of(
              UserId.generate(), new BigDecimal("4.5"), UserId.generate(), new BigDecimal("5.5"));
      final Map<UserId, BigDecimal> payers = Map.of(UserId.generate(), BigDecimal.TEN);

      final UpdateExpenseDbo dbo = UpdateExpenseDbo.builder().shares(shares).payers(payers).build();

      assertThat(dbo.shares()).isEqualTo(shares);
      assertThat(dbo.payers()).isEqualTo(payers);
    }

    @Test
    void stores_defensive_copies_of_shares_and_payers() {
      final UserId user = UserId.generate();
      final Map<UserId, BigDecimal> shares = new HashMap<>(Map.of(user, BigDecimal.TEN));
      final Map<UserId, BigDecimal> payers = new HashMap<>(Map.of(user, BigDecimal.TEN));

      final UpdateExpenseDbo dbo = UpdateExpenseDbo.builder().shares(shares).payers(payers).build();
      shares.clear();
      payers.clear();

      assertThat(dbo.shares()).containsOnlyKeys(user);
      assertThat(dbo.payers()).containsOnlyKeys(user);
    }
  }
}
