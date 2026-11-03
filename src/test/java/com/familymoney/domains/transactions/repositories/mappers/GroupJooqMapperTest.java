package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.generated.tables.records.GroupsRecord;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GroupJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID groupUuid = UUID.randomUUID();
      final String name = "Family";
      final String description = "Shared family expenses";
      final String currencyCode = "EUR";
      final GroupsRecord r = new GroupsRecord();
      r.setId(groupUuid);
      r.setName(name);
      r.setDescription(description);
      r.setCurrencyCode(currencyCode);

      final GroupEntity entity = GroupJooqMapper.toEntity(r);

      assertThat(entity.id()).isEqualTo(GroupId.fromUuid(groupUuid));
      assertThat(entity.name()).isEqualTo(GroupName.fromString(name));
      assertThat(entity.description()).isEqualTo(Description.of(description));
      assertThat(entity.currency()).isEqualTo(Currency.getInstance(currencyCode));
    }
  }
}
