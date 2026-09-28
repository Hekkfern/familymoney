package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.idempotency.services.IdempotencyService;
import com.familymoney.domains.idempotency.types.IdempotencyKey;
import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultPaymentController implements PaymentController {

  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  @Override
  public PageResponse<PaymentDto> getPayments(final UUID groupId, final int page, final int size) {
    // TODO
    return null;
  }

  @Override
  public CreatePaymentResponseDto createPayment(
      final String idempotencyKey,
      final UUID groupId,
      final CreatePaymentRequestDto request,
      HttpServletRequest httpRequest) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final IdempotencyKey key = IdempotencyKey.fromString(idempotencyKey);
    return idempotencyService.runWithIdempotency(
        key,
        user.id(),
        httpRequest,
        request,
        objectMapper.constructType(CreatePaymentResponseDto.class),
        () -> {
          // TODO
        });
  }

  @Override
  public PaymentDto getPayment(final UUID paymentId) {
    // TODO
    return null;
  }

  @Override
  public void updatePayment(final UUID paymentId, final UpdatePaymentRequestDto request) {
    // TODO
  }

  @Override
  public void deletePayment(final UUID paymentId) {
    // TODO
  }
}
