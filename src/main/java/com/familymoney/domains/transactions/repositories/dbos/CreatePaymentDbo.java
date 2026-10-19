package com.familymoney.domains.transactions.repositories.dbos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;

public record CreatePaymentDbo(
    PaymentId id,
    Description description,
    GroupId groupId,
    UserId createdBy,
    Instant doneAt,
    BigDecimal amount,
    UserId from,
    UserId to) {

  private static final int MAX_AMOUNT_SCALE = 3;

  public CreatePaymentDbo {
    if (from.equals(to)) {
      throw new IllegalArgumentException("Creditor and to must be different users");
    }
    final boolean isInvalidAmount = amount.signum() <= 0 || amount.scale() > MAX_AMOUNT_SCALE;
    if (isInvalidAmount) {
      throw new IllegalArgumentException("Invalid payment amount");
    }
  }
}
