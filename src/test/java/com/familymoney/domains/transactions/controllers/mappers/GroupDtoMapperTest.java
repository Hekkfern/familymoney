package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.GroupDto;
import com.familymoney.domains.transactions.services.data.GroupData;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.testutils.FakeGenerator;
import java.util.Currency;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GroupDtoMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final GroupId groupId = GroupId.generate();
      final String name = FakeGenerator.groupName();
      final String description = FakeGenerator.description();
      final Currency currency = Currency.getInstance("EUR");
      final GroupData data =
          new GroupData(groupId, GroupName.fromString(name), Description.of(description), currency);

      final GroupDto dto = GroupDtoMapper.toDto(data);

      assertThat(dto.id()).isEqualTo(groupId.value());
      assertThat(dto.name()).isEqualTo(name);
      assertThat(dto.description()).isEqualTo(description);
      assertThat(dto.currency()).isEqualTo(currency);
    }
  }
}
