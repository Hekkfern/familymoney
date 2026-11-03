package com.familymoney.domains.users.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.users.controllers.dtos.UserDto;
import com.familymoney.domains.users.services.data.UserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserDtoMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final UserId userId = UserId.generate();
      final String username = FakeGenerator.username();
      final String email = FakeGenerator.email();
      final UserData data =
          new UserData(userId, UserName.fromString(username), Email.fromString(email), true, false);

      final UserDto dto = UserDtoMapper.toDto(data);

      assertThat(dto.id()).isEqualTo(userId.value());
      assertThat(dto.username()).isEqualTo(username);
      assertThat(dto.email()).isEqualTo(email);
      assertThat(dto.isEmailVerified()).isTrue();
      assertThat(dto.isEnabled()).isFalse();
    }
  }
}
