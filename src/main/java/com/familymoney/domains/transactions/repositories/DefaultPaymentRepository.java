package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.exceptions.GroupNotFoundException;
import com.familymoney.domains.transactions.exceptions.PaymentNotFoundException;
import com.familymoney.domains.transactions.repositories.dbos.CreatePaymentDbo;
import com.familymoney.domains.transactions.repositories.dbos.UpdatePaymentDbo;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.mappers.PaymentJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.Keys;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.Payments;
import com.familymoney.utils.ConstraintViolationUtils;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DefaultPaymentRepository implements PaymentRepository {

  private final DSLContext db;

  @Override
  public void create(final CreatePaymentDbo dto) {
    try {
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
    } catch (final DataIntegrityViolationException exception) {
      throw toDomainException(exception, dto.groupId(), dto.from(), dto.to(), dto.createdBy());
    }
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

    final int updatedRows = executeUpdate(id, dto, values);
    if (updatedRows == 0) {
      final String msg = "Payment with ID '%s' does not exist".formatted(id.value());
      log.info(msg);
      throw new PaymentNotFoundException(msg);
    }
  }

  private int executeUpdate(
      final PaymentId id, final UpdatePaymentDbo dto, final Map<Field<?>, Object> values) {
    try {
      return db.update(Payments.PAYMENTS)
          .set(values)
          .where(Payments.PAYMENTS.ID.eq(id.value()))
          .execute();
    } catch (final DataIntegrityViolationException exception) {
      throw toDomainException(exception, null, dto.from(), dto.to(), null);
    }
  }

  private static RuntimeException toDomainException(
      final DataIntegrityViolationException exception,
      final @Nullable GroupId groupId,
      final @Nullable UserId creditor,
      final @Nullable UserId debitor,
      final @Nullable UserId createdBy) {
    final boolean isGroupMissing =
        groupId != null
            && ConstraintViolationUtils.isConstraintViolated(
                exception, Keys.PAYMENTS__PAYMENTS_GROUP_ID_FKEY);
    if (isGroupMissing) {
      final String msg = "Group with ID '%s' does not exist".formatted(groupId.value());
      log.info(msg);
      return new GroupNotFoundException(msg, exception);
    }
    final boolean isFromUserMissing =
        creditor != null
            && ConstraintViolationUtils.isConstraintViolated(
                exception, Keys.PAYMENTS__PAYMENTS_FROM_USER_FKEY);
    if (isFromUserMissing) {
      final String msg = "Creditor with ID '%s' does not exist".formatted(creditor.value());
      log.info(msg);
      return new UserNotFoundException(msg, exception);
    }
    final boolean isToUserMissing =
        debitor != null
            && ConstraintViolationUtils.isConstraintViolated(
                exception, Keys.PAYMENTS__PAYMENTS_TO_USER_FKEY);
    if (isToUserMissing) {
      final String msg = "Debitor with ID '%s' does not exist".formatted(debitor.value());
      log.info(msg);
      return new UserNotFoundException(msg, exception);
    }
    final boolean isCreatorMissing =
        createdBy != null
            && ConstraintViolationUtils.isConstraintViolated(
                exception, Keys.PAYMENTS__PAYMENTS_CREATED_BY_FKEY);
    if (isCreatorMissing) {
      final String msg = "Creator with ID '%s' does not exist".formatted(createdBy.value());
      log.info(msg);
      return new UserNotFoundException(msg, exception);
    }
    return exception;
  }

  @Override
  public void deleteById(final PaymentId id) {
    final int rowsAffected =
        db.deleteFrom(Payments.PAYMENTS).where(Payments.PAYMENTS.ID.eq(id.value())).execute();
    if (rowsAffected != 1) {
      log.warn("Could not delete payment with ID '{}'", id.value());
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
