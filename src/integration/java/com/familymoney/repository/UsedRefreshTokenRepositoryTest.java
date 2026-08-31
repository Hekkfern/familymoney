package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.repositories.DefaultUsedRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.UsedRefreshTokenRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateUsedRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.UsedRefreshTokenEntity;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.generated.tables.UsedRefreshTokens;
import com.familymoney.security.DefaultOpaqueTokenHasher;
import java.time.Instant;
import java.util.Optional;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class UsedRefreshTokenRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private UsedRefreshTokenRepository usedRefreshTokenRepository;

  @BeforeEach
  void setUp() {
    this.usedRefreshTokenRepository =
        new DefaultUsedRefreshTokenRepository(dslContext, new DefaultOpaqueTokenHasher());
  }

  @Nested
  class Create {

    @Test
    void create_persists_used_refresh_token() {
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final Instant now = Instant.now();

      final Optional<UsedRefreshTokenEntity> created =
          usedRefreshTokenRepository.create(new CreateUsedRefreshTokenDto(token, family, now));

      assertThat(created).isPresent();
      final UsedRefreshTokenEntity entry = created.get();
      assertThat(entry.family()).isEqualTo(family);
      assertThat(entry.usedAt()).isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(entry.createdAt()).isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(
              dslContext
                  .select(UsedRefreshTokens.USED_REFRESH_TOKENS.TOKEN_HASH)
                  .from(UsedRefreshTokens.USED_REFRESH_TOKENS)
                  .fetchSingle(UsedRefreshTokens.USED_REFRESH_TOKENS.TOKEN_HASH))
          .isEqualTo(new DefaultOpaqueTokenHasher().hash(token.value()))
          .isNotEqualTo(token.value());
    }

    @Test
    void create_returns_empty_when_token_has_already_been_used() {
      final RefreshToken token = RefreshToken.generate();
      final CreateUsedRefreshTokenDto dto =
          new CreateUsedRefreshTokenDto(token, TokenFamily.generate(), Instant.now());
      usedRefreshTokenRepository.create(dto);

      final Optional<UsedRefreshTokenEntity> created = usedRefreshTokenRepository.create(dto);

      assertThat(created).isEmpty();
    }
  }

  @Nested
  class FindByToken {

    @Test
    void returns_token_when_it_has_been_used() {
      final RefreshToken token = RefreshToken.generate();
      final UsedRefreshTokenEntity created =
          usedRefreshTokenRepository
              .create(new CreateUsedRefreshTokenDto(token, TokenFamily.generate(), Instant.now()))
              .orElseThrow();

      final UsedRefreshTokenEntity found =
          usedRefreshTokenRepository.findByToken(token).orElseThrow();

      assertThat(found).isEqualTo(created);
    }

    @Test
    void returns_empty_when_token_has_not_been_used() {
      assertThat(usedRefreshTokenRepository.findByToken(RefreshToken.generate())).isEmpty();
    }
  }
}
