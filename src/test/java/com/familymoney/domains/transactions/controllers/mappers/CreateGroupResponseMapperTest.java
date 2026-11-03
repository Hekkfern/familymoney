package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.CreateGroupResponseDto;
import com.familymoney.domains.transactions.types.GroupId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CreateGroupResponseMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final GroupId groupId = GroupId.generate();

      final CreateGroupResponseDto dto = CreateGroupResponseMapper.toDto(groupId);

      assertThat(dto.id()).isEqualTo(groupId.value());
    }
  }
}
