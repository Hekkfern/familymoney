package com.familymoney.repositories.mappers;

import com.familymoney.generated.tables.Groups;
import com.familymoney.repositories.entities.GroupEntity;
import com.familymoney.types.GroupId;
import com.familymoney.types.GroupName;
import java.time.OffsetDateTime;
import javax.money.Monetary;
import org.jooq.Record;

public final class GroupJooqMapper {

  private GroupJooqMapper() {}

  public static GroupEntity toEntity(final Record r) {
    OffsetDateTime createdAt = r.get(Groups.GROUPS.CREATED_AT);
    OffsetDateTime updatedAt = r.get(Groups.GROUPS.UPDATED_AT);

    return GroupEntity.builder()
        .id(GroupId.fromUuid(r.get(Groups.GROUPS.ID)))
        .name(GroupName.fromString(r.get(Groups.GROUPS.NAME)))
        .description(r.get(Groups.GROUPS.DESCRIPTION))
        .currency(Monetary.getCurrency(r.get(Groups.GROUPS.CURRENCY_CODE)))
        // The generated schema does not include a created_by column; set to null for now.
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .updatedAt(updatedAt != null ? updatedAt.toInstant() : null)
        .build();
  }
}
