package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
import com.familymoney.utils.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultPaymentController implements PaymentController {

  @Override
  public PageResponse<PaymentDto> getPayments(final UUID groupId, final int page, final int size) {
    return null;
  }

  @Override
  public void createPayment(
      final String idempotencyKey,
      final UUID groupId,
      final CreatePaymentRequestDto request,
      HttpServletRequest httpRequest) {}

  @Override
  public PaymentDto getPayment(final UUID paymentId) {
    return null;
  }

  @Override
  public void updatePayment(final UUID paymentId, final UpdatePaymentRequestDto request) {}

  @Override
  public void deletePayment(final UUID paymentId) {}
}
