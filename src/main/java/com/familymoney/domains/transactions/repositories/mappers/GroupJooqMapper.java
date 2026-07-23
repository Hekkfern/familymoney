package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.generated.tables.Groups;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.money.Monetary;
import org.jooq.Record;

public final class GroupJooqMapper {

  private GroupJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GroupEntity toEntity(final Record r) {
    OffsetDateTime createdAt = Objects.requireNonNull(r.get(Groups.GROUPS.CREATED_AT));
    OffsetDateTime updatedAt = Objects.requireNonNull(r.get(Groups.GROUPS.UPDATED_AT));

    return new GroupEntity(
        GroupId.fromUuid(r.get(Groups.GROUPS.ID)),
        GroupName.fromString(r.get(Groups.GROUPS.NAME)),
        Description.of(r.get(Groups.GROUPS.DESCRIPTION)),
        Monetary.getCurrency(r.get(Groups.GROUPS.CURRENCY_CODE)),
        createdAt.toInstant(),
        updatedAt.toInstant());
  }
}
