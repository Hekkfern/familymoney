package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.Payments;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.jooq.Record;

/** Maps payment database records to payment entities. */
public final class PaymentJooqMapper {

  private PaymentJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  /**
   * Maps a payment record to an entity.
   *
   * @param record the payment database record
   * @return the mapped payment entity
   */
  public static PaymentEntity toEntity(final Record record) {
    final OffsetDateTime doneAt = Objects.requireNonNull(record.get(Payments.PAYMENTS.DONE_AT));
    final OffsetDateTime createdAt =
        Objects.requireNonNull(record.get(Payments.PAYMENTS.CREATED_AT));
    final OffsetDateTime updatedAt =
        Objects.requireNonNull(record.get(Payments.PAYMENTS.UPDATED_AT));

    return new PaymentEntity(
        PaymentId.fromUuid(record.get(Payments.PAYMENTS.ID)),
        Description.of(record.get(Payments.PAYMENTS.DESCRIPTION)),
        Money.of(
            record.get(Payments.PAYMENTS.AMOUNT),
            Monetary.getCurrency(record.get(Payments.PAYMENTS.CURRENCY_CODE))),
        UserId.fromUuid(record.get(Payments.PAYMENTS.CREDITOR)),
        UserId.fromUuid(record.get(Payments.PAYMENTS.DEBITOR)),
        GroupId.fromUuid(record.get(Payments.PAYMENTS.GROUP_ID)),
        UserId.fromUuid(record.get(Payments.PAYMENTS.CREATED_BY)),
        doneAt.toInstant(),
        createdAt.toInstant(),
        updatedAt.toInstant());
  }
}
