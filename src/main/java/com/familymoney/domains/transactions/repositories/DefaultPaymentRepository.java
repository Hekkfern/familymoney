package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dtos.CreatePaymentDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdatePaymentDto;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.exceptions.CreatePaymentException;
import com.familymoney.domains.transactions.repositories.exceptions.DeletePaymentException;
import com.familymoney.domains.transactions.repositories.exceptions.UpdatePaymentException;
import com.familymoney.domains.transactions.repositories.mappers.PaymentJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.Payments;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultPaymentRepository implements PaymentRepository {

  private final DSLContext db;

  @Transactional
  @Override
  public void create(final CreatePaymentDto dto) {
    final String currencyCode = fetchGroupCurrencyCode(dto.groupId());
    if (!currencyCode.equals(dto.amount().getCurrency().getCurrencyCode())) {
      throw new CreatePaymentException(
          "Payment in Group '%s' must use currency: %s"
              .formatted(dto.groupId().value(), currencyCode));
    }

    final int paymentsCreated =
        db.insertInto(Payments.PAYMENTS)
            .columns(
                Payments.PAYMENTS.ID,
                Payments.PAYMENTS.GROUP_ID,
                Payments.PAYMENTS.DESCRIPTION,
                Payments.PAYMENTS.AMOUNT,
                Payments.PAYMENTS.CURRENCY_CODE,
                Payments.PAYMENTS.CREDITOR,
                Payments.PAYMENTS.DEBITOR,
                Payments.PAYMENTS.DONE_AT,
                Payments.PAYMENTS.CREATED_BY)
            .values(
                dto.id().value(),
                dto.groupId().value(),
                dto.description().value(),
                dto.amount().getNumber().numberValue(BigDecimal.class),
                dto.amount().getCurrency().getCurrencyCode(),
                dto.creditor().value(),
                dto.debitor().value(),
                OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET),
                dto.createdBy().value())
            .execute();
    if (paymentsCreated != 1) {
      throw new CreatePaymentException(
          "Could not create payment with ID: %s".formatted(dto.id().value()));
    }
  }

  @Transactional
  @Override
  public void updateById(final PaymentId id, final UpdatePaymentDto dto) {
    if (dto.isEmpty()) {
      return;
    }
    if (dto.amount() != null) {
      validatePaymentCurrency(id, dto.amount());
    }

    final Map<Field<?>, Object> values = new LinkedHashMap<>();
    if (dto.description() != null) {
      values.put(Payments.PAYMENTS.DESCRIPTION, dto.description().value());
    }
    if (dto.doneAt() != null) {
      values.put(
          Payments.PAYMENTS.DONE_AT,
          OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET));
    }
    if (dto.amount() != null) {
      values.put(Payments.PAYMENTS.AMOUNT, dto.amount().getNumber().numberValue(BigDecimal.class));
    }
    if (dto.creditor() != null) {
      values.put(Payments.PAYMENTS.CREDITOR, dto.creditor().value());
    }
    if (dto.debitor() != null) {
      values.put(Payments.PAYMENTS.DEBITOR, dto.debitor().value());
    }

    final int updatedRows =
        db.update(Payments.PAYMENTS)
            .set(values)
            .where(Payments.PAYMENTS.ID.eq(id.value()))
            .execute();
    if (updatedRows != 1) {
      throw new UpdatePaymentException("Could not update payment ID: %s".formatted(id.value()));
    }
  }

  @Override
  public void deleteById(final PaymentId id) {
    final int rowsAffected =
        db.deleteFrom(Payments.PAYMENTS).where(Payments.PAYMENTS.ID.eq(id.value())).execute();
    if (rowsAffected == 0) {
      throw new DeletePaymentException("Could not delete payment ID: %s".formatted(id.value()));
    }
  }

  @Override
  public Optional<PaymentEntity> findById(final PaymentId id) {
    return db.selectFrom(Payments.PAYMENTS)
        .where(Payments.PAYMENTS.ID.eq(id.value()))
        .fetchOptional(PaymentJooqMapper::toEntity);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<PaymentEntity> findAllByGroupId(
      final GroupId groupId, final int page, final int size) {
    final PageRequest pageable = PageRequest.of(page, size);
    final Long total =
        db.selectCount()
            .from(Payments.PAYMENTS)
            .where(Payments.PAYMENTS.GROUP_ID.eq(groupId.value()))
            .fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;
    final List<PaymentEntity> data =
        db.selectFrom(Payments.PAYMENTS)
            .where(Payments.PAYMENTS.GROUP_ID.eq(groupId.value()))
            .orderBy(Payments.PAYMENTS.DONE_AT.desc())
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch(PaymentJooqMapper::toEntity);
    return new PageImpl<>(data, pageable, safeTotal);
  }

  private String fetchGroupCurrencyCode(final GroupId groupId) {
    return db.select(Groups.GROUPS.CURRENCY_CODE)
        .from(Groups.GROUPS)
        .where(Groups.GROUPS.ID.eq(groupId.value()))
        .fetchOptional(Groups.GROUPS.CURRENCY_CODE)
        .orElseThrow(
            () ->
                new CreatePaymentException(
                    "Could not find group with ID: %s".formatted(groupId.value())));
  }

  private void validatePaymentCurrency(final PaymentId id, final Money amount) {
    final String currencyCode =
        db.select(Payments.PAYMENTS.CURRENCY_CODE)
            .from(Payments.PAYMENTS)
            .where(Payments.PAYMENTS.ID.eq(id.value()))
            .fetchOptional(Payments.PAYMENTS.CURRENCY_CODE)
            .orElseThrow(
                () ->
                    new UpdatePaymentException(
                        "Could not find payment with ID: %s".formatted(id.value())));
    if (!currencyCode.equals(amount.getCurrency().getCurrencyCode())) {
      throw new UpdatePaymentException(
          "Payment %s must use currency: %s".formatted(id.value(), currencyCode));
    }
  }
}
