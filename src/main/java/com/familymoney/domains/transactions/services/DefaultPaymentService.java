package com.familymoney.domains.transactions.services;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.PaymentNotFoundException;
import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.PaymentRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreatePaymentDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdatePaymentDto;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.services.data.PaymentData;
import com.familymoney.domains.transactions.services.data.UpdatePaymentData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.utils.AuthorizedUser;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultPaymentService implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final GroupRepository groupRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<PaymentData> getPayments(
      final GroupId groupId, final AuthorizedUser requester, final int page, int size) {
    final Currency currency = getGroupCurrency(groupId);
    return paymentRepository
        .findAllByGroupId(groupId, PageRequest.of(page, size))
        .map(entity -> toPaymentData(entity, currency));
  }

  @Override
  public PaymentId createPayment(
      final GroupId groupId,
      final Description description,
      final BigDecimal amount,
      final Instant doneAt,
      final UserId from,
      final UserId to,
      final AuthorizedUser requester) {
    final PaymentId id = PaymentId.generate();
    paymentRepository.create(
        new CreatePaymentDto(id, description, groupId, requester.id(), doneAt, amount, to, from));
    return id;
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentData getPayment(final PaymentId id, final AuthorizedUser requester) {
    final PaymentEntity entity =
        paymentRepository
            .findById(id)
            .orElseThrow(
                () -> {
                  final String message =
                      "Payment with ID '%s' does not exist".formatted(id.value());
                  log.info(message);
                  return new PaymentNotFoundException(message);
                });
    return toPaymentData(entity, getGroupCurrency(entity.groupId()));
  }

  @Override
  public void updatePayment(
      final PaymentId id, final AuthorizedUser requester, final UpdatePaymentData data) {
    paymentRepository.updateById(
        id,
        new UpdatePaymentDto(
            data.description(), data.doneAt(), data.amount(), data.to(), data.from()));
  }

  @Override
  public void deletePayment(final PaymentId id, final AuthorizedUser requester) {
    paymentRepository.deleteById(id);
  }

  private Currency getGroupCurrency(final GroupId groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(
            () -> {
              final String message = "Group with ID '%s' does not exist".formatted(groupId.value());
              log.info(message);
              return new GroupNotFoundException(message);
            })
        .currency();
  }

  private static PaymentData toPaymentData(final PaymentEntity entity, final Currency currency) {
    return new PaymentData(
        entity.id(),
        entity.groupId(),
        entity.description(),
        Money.of(entity.amount(), currency.getCurrencyCode()),
        entity.doneAt(),
        entity.debitor(),
        entity.creditor());
  }
}
