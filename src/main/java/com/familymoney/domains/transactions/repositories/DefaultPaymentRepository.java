package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dbos.CreatePaymentDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdatePaymentDbo;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.mappers.PaymentJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.Payments;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DefaultPaymentRepository implements PaymentRepository {

  private final DSLContext db;

  @Override
  public void create(final CreatePaymentDbo dto) {
    db.insertInto(Payments.PAYMENTS)
        .columns(
            Payments.PAYMENTS.ID,
            Payments.PAYMENTS.GROUP_ID,
            Payments.PAYMENTS.DESCRIPTION,
            Payments.PAYMENTS.AMOUNT,
            Payments.PAYMENTS.FROM_USER,
            Payments.PAYMENTS.TO_USER,
            Payments.PAYMENTS.DONE_AT,
            Payments.PAYMENTS.CREATED_BY)
        .values(
            dto.id().value(),
            dto.groupId().value(),
            dto.description().value(),
            dto.amount(),
            dto.from().value(),
            dto.to().value(),
            OffsetDateTime.ofInstant(dto.doneAt(), DEFAULT_TIMEZONE_OFFSET),
            dto.createdBy().value())
        .execute();
  }

  @Override
  public void updateById(final PaymentId id, final UpdatePaymentDbo dto) {
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
      values.put(Payments.PAYMENTS.AMOUNT, dto.amount());
    }
    if (dto.from() != null) {
      values.put(Payments.PAYMENTS.FROM_USER, dto.from().value());
    }
    if (dto.to() != null) {
      values.put(Payments.PAYMENTS.TO_USER, dto.to().value());
    }
    db.update(Payments.PAYMENTS).set(values).where(Payments.PAYMENTS.ID.eq(id.value())).execute();
  }

  @Override
  public void deleteById(final PaymentId id) {
    db.deleteFrom(Payments.PAYMENTS).where(Payments.PAYMENTS.ID.eq(id.value())).execute();
  }

  @Override
  public Optional<PaymentEntity> findById(final PaymentId id) {
    return db.selectFrom(Payments.PAYMENTS)
        .where(Payments.PAYMENTS.ID.eq(id.value()))
        .fetchOptional(PaymentJooqMapper::toEntity);
  }

  @Override
  public List<PaymentEntity> findAllByGroupIdAndIds(
      final GroupId groupId, final Set<PaymentId> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    return db.select(
            Payments.PAYMENTS.ID,
            Payments.PAYMENTS.DESCRIPTION,
            Payments.PAYMENTS.AMOUNT,
            Payments.PAYMENTS.FROM_USER,
            Payments.PAYMENTS.TO_USER,
            Payments.PAYMENTS.GROUP_ID,
            Payments.PAYMENTS.DONE_AT)
        .from(Payments.PAYMENTS)
        .where(Payments.PAYMENTS.GROUP_ID.eq(groupId.value()))
        .and(Payments.PAYMENTS.ID.in(ids.stream().map(PaymentId::value).toList()))
        .fetch(PaymentJooqMapper::toEntity);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<PaymentEntity> findAllByGroupId(final GroupId groupId, final Pageable pageable) {
    final Long total =
        db.selectCount()
            .from(Payments.PAYMENTS)
            .where(Payments.PAYMENTS.GROUP_ID.eq(groupId.value()))
            .fetchOne(0, Long.class);
    final long safeTotal = total != null ? total : 0L;

    final List<SortField<?>> orderFields =
        pageable.getSort().stream()
            .map(
                order -> {
                  final Field<?> field = Payments.PAYMENTS.field(order.getProperty());
                  if (field == null) {
                    throw new IllegalArgumentException(
                        "Unknown sort field: " + order.getProperty());
                  }
                  return order.isAscending() ? field.asc() : field.desc();
                })
            .toList();

    final List<SortField<?>> effectiveOrder =
        orderFields.isEmpty()
            ? List.of(Payments.PAYMENTS.DONE_AT.desc(), Payments.PAYMENTS.ID.desc())
            : orderFields;

    final List<PaymentEntity> data =
        db.selectFrom(Payments.PAYMENTS)
            .where(Payments.PAYMENTS.GROUP_ID.eq(groupId.value()))
            .orderBy(effectiveOrder)
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch(PaymentJooqMapper::toEntity);
    return new PageImpl<>(data, pageable, safeTotal);
  }

  @Override
  public int deletePaymentsOfPurgeableGroups(final Instant deletedBefore, final int batchSize) {
    return db.deleteFrom(Payments.PAYMENTS)
        .where(
            Payments.PAYMENTS.ID.in(
                db.select(Payments.PAYMENTS.ID)
                    .from(Payments.PAYMENTS)
                    .join(Groups.GROUPS)
                    .on(Groups.GROUPS.ID.eq(Payments.PAYMENTS.GROUP_ID))
                    .where(
                        Groups.GROUPS.DELETED_AT.lt(
                            deletedBefore.atOffset(DEFAULT_TIMEZONE_OFFSET)))
                    .limit(batchSize)))
        .execute();
  }
}
