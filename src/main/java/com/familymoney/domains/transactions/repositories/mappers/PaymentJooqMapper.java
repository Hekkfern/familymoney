package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.Payments;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

/** Maps payment database records to payment entities. */
public final class PaymentJooqMapper {

  private PaymentJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  /**
   * Maps a payment record to an entity.
   *
   * @param r the payment database record
   * @return the mapped payment entity
   */
  public static PaymentEntity toEntity(final Record r) {
    final OffsetDateTime doneAt = Objects.requireNonNull(r.get(Payments.PAYMENTS.DONE_AT));

    return new PaymentEntity(
        PaymentId.fromUuid(r.get(Payments.PAYMENTS.ID)),
        Description.of(r.get(Payments.PAYMENTS.DESCRIPTION)),
        r.get(Payments.PAYMENTS.AMOUNT),
        UserId.fromUuid(r.get(Payments.PAYMENTS.FROM_USER)),
        UserId.fromUuid(r.get(Payments.PAYMENTS.TO_USER)),
        GroupId.fromUuid(r.get(Payments.PAYMENTS.GROUP_ID)),
        doneAt.toInstant());
  }
}
