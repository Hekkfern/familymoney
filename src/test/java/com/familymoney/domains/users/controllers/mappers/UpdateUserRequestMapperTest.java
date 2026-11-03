package com.familymoney.domains.users.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.users.controllers.dtos.UpdateUserRequestDto;
import com.familymoney.domains.users.services.data.UpdateUserData;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Password;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UpdateUserRequestMapperTest {

  @Nested
  class FromDto {

    @Test
    void maps_successfully() {
      final String username = FakeGenerator.username();
      final String email = FakeGenerator.email();
      final String password = FakeGenerator.password();
      final UpdateUserRequestDto dto = new UpdateUserRequestDto(username, email, password);

      final UpdateUserData data = UpdateUserRequestMapper.fromDto(dto);

      assertThat(data.username()).isEqualTo(UserName.fromString(username));
      assertThat(data.email()).isEqualTo(Email.fromString(email));
      assertThat(data.password()).isEqualTo(Password.fromString(password));
    }

    @Test
    void preserves_username_only_update() {
      final String username = FakeGenerator.username();
      final UpdateUserRequestDto dto = new UpdateUserRequestDto(username, null, null);

      final UpdateUserData data = UpdateUserRequestMapper.fromDto(dto);

      assertThat(data.username()).isEqualTo(UserName.fromString(username));
      assertThat(data.email()).isNull();
      assertThat(data.password()).isNull();
    }

    @Test
    void preserves_email_only_update() {
      final String email = FakeGenerator.email();
      final UpdateUserRequestDto dto = new UpdateUserRequestDto(null, email, null);

      final UpdateUserData data = UpdateUserRequestMapper.fromDto(dto);

      assertThat(data.username()).isNull();
      assertThat(data.email()).isEqualTo(Email.fromString(email));
      assertThat(data.password()).isNull();
    }

    @Test
    void preserves_password_only_update() {
      final String password = FakeGenerator.password();
      final UpdateUserRequestDto dto = new UpdateUserRequestDto(null, null, password);

      final UpdateUserData data = UpdateUserRequestMapper.fromDto(dto);

      assertThat(data.username()).isNull();
      assertThat(data.email()).isNull();
      assertThat(data.password()).isEqualTo(Password.fromString(password));
    }

    @Test
    void throws_when_no_field_is_provided() {
      final UpdateUserRequestDto dto = new UpdateUserRequestDto(null, null, null);

      assertThatThrownBy(() -> UpdateUserRequestMapper.fromDto(dto))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
