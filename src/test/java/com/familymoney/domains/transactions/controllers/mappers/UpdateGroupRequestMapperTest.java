package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.UpdateGroupRequestDto;
import com.familymoney.domains.transactions.services.data.UpdateGroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateGroupRequestMapperTest {

  @Nested
  class FromDto {

    @Test
    void maps_successfully() {
      final String name = FakeGenerator.groupName();
      final String description = FakeGenerator.description();
      final UpdateGroupRequestDto dto = new UpdateGroupRequestDto(name, description);

      final UpdateGroupData data = UpdateGroupRequestMapper.fromDto(dto);

      assertThat(data.name()).isEqualTo(GroupName.fromString(name));
      assertThat(data.description()).isEqualTo(Description.of(description));
    }
  }
}
