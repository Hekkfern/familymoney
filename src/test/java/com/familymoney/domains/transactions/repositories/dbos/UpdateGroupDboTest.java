package com.familymoney.domains.transactions.repositories.dbos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateGroupDboTest {

  @Nested
  class Constructor {

    @Test
    void rejects_update_without_any_field() {
      assertThatIllegalArgumentException().isThrownBy(() -> new UpdateGroupDbo(null, null));
    }

    @Test
    void accepts_update_with_only_name() {
      final GroupName name = GroupName.fromString(FakeGenerator.groupName());

      final UpdateGroupDbo dbo = new UpdateGroupDbo(name, null);

      assertThat(dbo.name()).isEqualTo(name);
    }

    @Test
    void accepts_update_with_only_description() {
      final Description description = Description.of(FakeGenerator.description());

      final UpdateGroupDbo dbo = new UpdateGroupDbo(null, description);

      assertThat(dbo.description()).isEqualTo(description);
    }
  }
}
