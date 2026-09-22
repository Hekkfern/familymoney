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
import com.familymoney.generated.tables.Payments;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultPaymentRepository implements PaymentRepository {

  private final DSLContext db;

  @Override
  public void create(final CreatePaymentDto dto) {
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

  @Override
  public void updateById(final PaymentId id, final UpdatePaymentDto dto) {
    if (dto.isEmpty()) {
      return;
    }
    validatePaymentParties(id, dto);

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

  private void validatePaymentParties(final PaymentId id, final UpdatePaymentDto data) {
    if (data.creditor() == null && data.debitor() == null) {
      return;
    }
    final Optional<Record2<java.util.UUID, java.util.UUID>> paymentParties =
        db.select(Payments.PAYMENTS.CREDITOR, Payments.PAYMENTS.DEBITOR)
            .from(Payments.PAYMENTS)
            .where(Payments.PAYMENTS.ID.eq(id.value()))
            .fetchOptional();
    if (paymentParties.isEmpty()) {
      throw new UpdatePaymentException("Could not find payment with ID: %s".formatted(id.value()));
    }
    final Record2<java.util.UUID, java.util.UUID> currentParties = paymentParties.get();
    final java.util.UUID creditor =
        data.creditor() != null
            ? data.creditor().value()
            : currentParties.get(Payments.PAYMENTS.CREDITOR);
    final java.util.UUID debitor =
        data.debitor() != null
            ? data.debitor().value()
            : currentParties.get(Payments.PAYMENTS.DEBITOR);
    if (creditor.equals(debitor)) {
      throw new UpdatePaymentException("Payment creditor and debitor must be different");
    }
  }
}
