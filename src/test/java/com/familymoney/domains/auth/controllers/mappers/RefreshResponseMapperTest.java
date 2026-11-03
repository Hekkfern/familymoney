package com.familymoney.domains.auth.controllers.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.controllers.dtos.RefreshResponseDto;
import com.familymoney.domains.auth.services.data.TokenPair;
import com.familymoney.domains.auth.types.AccessToken;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.testutils.FakeGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RefreshResponseMapperTest {

  @Nested
  class ToDto {

    @Test
    void maps_successfully() {
      final String accessToken = FakeGenerator.accessToken();
      final String refreshToken = FakeGenerator.refreshToken();
      final TokenPair tokenPair =
          new TokenPair(AccessToken.fromString(accessToken), RefreshToken.fromString(refreshToken));

      final RefreshResponseDto dto = RefreshResponseMapper.toDto(tokenPair);

      assertThat(dto.accessToken()).isEqualTo(accessToken);
      assertThat(dto.refreshToken()).isEqualTo(refreshToken);
    }
  }
}
