package com.familymoney.domains.transactions.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.transactions.controllers.dtos.MemberDto;
import com.familymoney.domains.transactions.services.data.MemberData;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberDtoMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final UserId userId = UserId.generate();
      final String username = FakeGenerator.username();
      final MemberData data = new MemberData(userId, UserName.fromString(username));

      final MemberDto dto = MemberDtoMapper.toDto(data);

      assertThat(dto.id()).isEqualTo(userId.value());
      assertThat(dto.username()).isEqualTo(username);
    }
  }
}
