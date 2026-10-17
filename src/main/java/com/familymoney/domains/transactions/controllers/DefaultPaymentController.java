package com.familymoney.domains.transactions.controllers;

import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentRequestDto;
import com.familymoney.domains.transactions.controllers.dtos.CreatePaymentResponseDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentDto;
import com.familymoney.domains.transactions.controllers.dtos.UpdatePaymentRequestDto;
import com.familymoney.domains.transactions.controllers.mappers.PaymentDtoMapper;
import com.familymoney.domains.transactions.controllers.mappers.UpdatePaymentRequestMapper;
import com.familymoney.domains.transactions.services.PaymentService;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthenticationUtils;
import com.familymoney.utils.AuthorizedUser;
import com.familymoney.utils.PageResponse;
import com.familymoney.utils.PageResponseMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DefaultPaymentController implements PaymentController {

  private final PaymentService paymentService;

  @Override
  public PageResponse<PaymentDto> getPayments(final UUID groupId, final int page, final int size) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final Page<PaymentData> paymentPages =
        paymentService.getPayments(GroupId.fromUuid(groupId), user, page, size);
    return PageResponseMapper.from(paymentPages.map(PaymentDtoMapper::toDto));
  }

  @Override
  public CreatePaymentResponseDto createPayment(
      final UUID groupId, final CreatePaymentRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final PaymentId paymentId =
        paymentService.createPayment(
            GroupId.fromUuid(groupId),
            Description.of(request.description()),
            request.amount(),
            request.doneAt(),
            UserId.fromUuid(request.from()),
            UserId.fromUuid(request.to()),
            user);
    return new CreatePaymentResponseDto(paymentId.value());
  }

  @Override
  public PaymentDto getPayment(final UUID paymentId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    final PaymentData payment = paymentService.getPayment(PaymentId.fromUuid(paymentId), user);
    return PaymentDtoMapper.toDto(payment);
  }

  @Override
  public void updatePayment(final UUID paymentId, final UpdatePaymentRequestDto request) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    paymentService.updatePayment(
        PaymentId.fromUuid(paymentId), user, UpdatePaymentRequestMapper.fromDto(request));
  }

  @Override
  public void deletePayment(final UUID paymentId) {
    final AuthorizedUser user = AuthenticationUtils.getAuthorizedUserFromSecurityContext();
    paymentService.deletePayment(PaymentId.fromUuid(paymentId), user);
  }
}
