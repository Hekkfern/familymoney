package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.UpdatePaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Page;

public interface PaymentService {

  Page<PaymentData> getPayments(final GroupId groupId, final int page, int size);

  PaymentId createPayment(
      final GroupId groupId,
      final Description description,
      final BigDecimal amount,
      final Instant doneAt,
      final UserId from,
      final UserId to,
      final UserId createdBy);

  PaymentData getPayment(final PaymentId id);

  void updatePayment(final PaymentId id, final UpdatePaymentData data);

  void deletePayment(final PaymentId id);
}
