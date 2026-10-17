package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.UpdatePaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.Page;

public interface PaymentService {

  Page<PaymentData> getPayments(GroupId groupId, AuthorizedUser requester, int page, int size);

  PaymentId createPayment(
      GroupId groupId,
      Description description,
      BigDecimal amount,
      Instant doneAt,
      UserId from,
      UserId to,
      AuthorizedUser requester);

  PaymentData getPayment(PaymentId id, AuthorizedUser requester);

  void updatePayment(PaymentId id, AuthorizedUser requester, UpdatePaymentData data);

  void deletePayment(PaymentId id, AuthorizedUser requester);
}
