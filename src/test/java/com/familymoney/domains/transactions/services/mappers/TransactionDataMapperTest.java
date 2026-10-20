package com.familymoney.domains.transactions.services.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpensePaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.ExpenseShareEntity;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TransactionDataMapperTest {

  private static final Instant DONE_AT = Instant.parse("2026-01-01T12:00:00Z");
  private final Currency currency = Currency.getInstance("EUR");
  private final GroupId groupId = GroupId.generate();
  private final UserId sender = UserId.generate();
  private final UserId recipient = UserId.generate();

  @Nested
  class FromExpense {

    @Test
    void maps_the_complete_expense_and_calculates_the_total_from_shares() {
      final ExpenseEntity expense =
          new ExpenseEntity(ExpenseId.generate(), Description.of("Dinner"), groupId, DONE_AT);
      final FullExpenseEntity entity =
          new FullExpenseEntity(
              expense,
              List.of(
                  new ExpenseShareEntity(sender, new BigDecimal("2.345")),
                  new ExpenseShareEntity(recipient, new BigDecimal("10.000"))),
              List.of(new ExpensePaymentEntity(sender, new BigDecimal("12.345"))));

      final TransactionData.Expense result = TransactionDataMapper.fromExpense(entity, currency);

      assertThat(result.expense().id()).isEqualTo(expense.id());
      assertThat(result.expense().groupId()).isEqualTo(groupId);
      assertThat(result.expense().description()).isEqualTo(expense.description());
      assertThat(result.expense().doneAt()).isEqualTo(DONE_AT);
      assertThat(result.expense().totalAmount().getCurrency().getCurrencyCode()).isEqualTo("EUR");
      assertThat(result.expense().totalAmount().getNumber().numberValueExact(BigDecimal.class))
          .isEqualByComparingTo("12.345");
      assertThat(result.expense().shares())
          .hasSize(2)
          .containsEntry(sender, new BigDecimal("2.345"))
          .containsEntry(recipient, new BigDecimal("10.000"));
      assertThat(result.expense().payers()).containsEntry(sender, new BigDecimal("12.345"));
    }
  }

  @Nested
  class FromPayment {

    @Test
    void maps_the_complete_payment() {
      final PaymentEntity payment =
          new PaymentEntity(
              PaymentId.generate(),
              Description.of("Settlement"),
              new BigDecimal("7.125"),
              sender,
              recipient,
              groupId,
              DONE_AT);

      final TransactionData.Payment result = TransactionDataMapper.fromPayment(payment, currency);

      assertThat(result.payment().id()).isEqualTo(payment.id());
      assertThat(result.payment().groupId()).isEqualTo(groupId);
      assertThat(result.payment().description()).isEqualTo(payment.description());
      assertThat(result.payment().doneAt()).isEqualTo(DONE_AT);
      assertThat(result.payment().money().getCurrency().getCurrencyCode()).isEqualTo("EUR");
      assertThat(result.payment().money().getNumber().numberValueExact(BigDecimal.class))
          .isEqualByComparingTo("7.125");
      assertThat(result.payment().from()).isEqualTo(sender);
      assertThat(result.payment().to()).isEqualTo(recipient);
    }
  }
}
