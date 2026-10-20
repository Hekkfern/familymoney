package com.familymoney.domains.transactions.controllers.mappers;

import com.familymoney.domains.transactions.controllers.dtos.ExpenseTransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentTransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.services.data.ExpenseData;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.TransactionData;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Maps service transactions to their HTTP response variants. */
public final class TransactionDtoMapper {

  private TransactionDtoMapper() {
    /* this class is not intended to be instantiated */
  }

  /**
   * Maps a transaction to the matching response variant.
   *
   * @param data the complete expense or payment data
   * @return the response with transaction details and completion time
   */
  public static TransactionDto toDto(final TransactionData data) {
    return switch (data) {
      case TransactionData.Payment(final PaymentData payment) -> {
        yield new PaymentTransactionDto(
            payment.id().value(),
            payment.money().getNumber().numberValueExact(BigDecimal.class),
            payment.description().value(),
            payment.doneAt(),
            payment.from().value(),
            payment.to().value());
      }

      case TransactionData.Expense(final ExpenseData expense) -> {
        final List<UUID> payers =
            expense.payers().keySet().stream().map(UserId::value).sorted().toList();
        final List<UUID> shares =
            expense.shares().keySet().stream().map(UserId::value).sorted().toList();

        yield new ExpenseTransactionDto(
            expense.id().value(),
            expense.totalAmount().getNumber().numberValueExact(BigDecimal.class),
            expense.description().value(),
            expense.doneAt(),
            payers,
            shares);
      }
    };
  }
}
