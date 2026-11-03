package com.familymoney.domains.transactions.repositories.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.repositories.entitites.ExpenseEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.generated.tables.records.ExpensesRecord;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpenseJooqMapperTest {

  @Nested
  class ToEntity {

    @Test
    void map_successfully() {
      final UUID expenseUuid = UUID.randomUUID();
      final UUID groupUuid = UUID.randomUUID();
      final String description = "Dinner";
      final OffsetDateTime doneAt =
          OffsetDateTime.of(2026, 1, 2, 10, 0, 0, 0, ZoneOffset.ofHours(1));
      final ExpensesRecord r = new ExpensesRecord();
      r.setId(expenseUuid);
      r.setDescription(description);
      r.setGroupId(groupUuid);
      r.setDoneAt(doneAt);

      final ExpenseEntity entity = ExpenseJooqMapper.toEntity(r);

      assertThat(entity.id()).isEqualTo(ExpenseId.fromUuid(expenseUuid));
      assertThat(entity.description()).isEqualTo(Description.of(description));
      assertThat(entity.groupId()).isEqualTo(GroupId.fromUuid(groupUuid));
      assertThat(entity.doneAt()).isEqualTo(doneAt.toInstant());
    }
  }
}
