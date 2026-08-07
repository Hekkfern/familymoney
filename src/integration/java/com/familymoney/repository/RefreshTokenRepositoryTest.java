package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.auth.repositories.RefreshTokenRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.entitites.RefreshTokenEntity;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.RefreshTokens;
import com.familymoney.security.DefaultOpaqueTokenHasher;
import com.familymoney.test_utils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class RefreshTokenRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private RefreshTokenRepository refreshTokenRepository;
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    this.refreshTokenRepository =
        new RefreshTokenRepository(dslContext, new DefaultOpaqueTokenHasher());
    this.databaseCrud = new DatabaseCrud(dslContext);
  }

  private UserId insertRandomUser() {
    final UserId userId = UserId.generate();
    final Instant now = Instant.ofEpochSecond(1778755330);
    databaseCrud.insertUser(
        userId,
        UserName.fromString(FakeGenerator.username()),
        Email.fromString(FakeGenerator.email()),
        "hashed_password",
        now,
        true,
        true);
    return userId;
  }

  @Nested
  class Create {

    @Test
    void persists_refresh_token_record() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();

      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));

      final Optional<RefreshTokenEntity> refreshTokenCreated =
          refreshTokenRepository.create(
              new CreateRefreshTokenDto(UUID.randomUUID(), userId, token, family, expiration));

      assertThat(refreshTokenCreated).isPresent();
      final RefreshTokenEntity refreshToken = refreshTokenCreated.get();
      assertThat(refreshToken.id()).isNotNull();
      assertThat(refreshToken.userId()).isNotNull().isEqualTo(userId);
      assertThat(refreshToken.createdAt())
          .isNotNull()
          .isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(refreshToken.updatedAt())
          .isNotNull()
          .isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(refreshToken.expiresAt().value())
          .isNotNull()
          .isBetween(expiration.value().minusSeconds(1), expiration.value().plusSeconds(1));
      assertThat(refreshToken.family()).isNotNull().isEqualTo(family);
      assertThat(
              dslContext
                  .select(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH)
                  .from(RefreshTokens.REFRESH_TOKENS)
                  .fetchSingle(RefreshTokens.REFRESH_TOKENS.TOKEN_HASH))
          .isEqualTo(new DefaultOpaqueTokenHasher().hash(token.value()))
          .isNotEqualTo(token.value());
    }

    void persists_when_same_user_but_different_family() {
      // TODO
    }

    @Test
    void throws_when_user_does_not_exist() {
      final UserId missingUserId = UserId.generate();

      final Instant now = Instant.now();
      final ExpirationTime expiresAt = ExpirationTime.of(now);

      final CreateRefreshTokenDto dto =
          new CreateRefreshTokenDto(
              UUID.randomUUID(),
              missingUserId,
              RefreshToken.generate(),
              TokenFamily.generate(),
              expiresAt);
      assertThatThrownBy(() -> refreshTokenRepository.create(dto))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throws_when_token_is_duplicate() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();

      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));

      databaseCrud.insertRefreshToken(
          UUID.randomUUID(), userId, token, now, expiration, TokenFamily.generate());

      final CreateRefreshTokenDto dto =
          new CreateRefreshTokenDto(
              UUID.randomUUID(), userId, token, TokenFamily.generate(), expiration);
      assertThatThrownBy(() -> refreshTokenRepository.create(dto))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throws_when_user_id_and_family_are_duplicate() {
      final UserId userId = insertRandomUser();
      final TokenFamily family = TokenFamily.generate();

      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));

      databaseCrud.insertRefreshToken(
          UUID.randomUUID(), userId, RefreshToken.generate(), now, expiration, family);

      final CreateRefreshTokenDto dto =
          new CreateRefreshTokenDto(
              UUID.randomUUID(), userId, RefreshToken.generate(), family, expiration);
      assertThatThrownBy(() -> refreshTokenRepository.create(dto))
          .isInstanceOf(DuplicateKeyException.class);
    }
  }

  @Nested
  class FindByToken {

    @Test
    void returns_token_when_exists() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final UUID id = UUID.randomUUID();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertRefreshToken(id, userId, token, now, expiration, family);

      final Optional<RefreshTokenEntity> tokenFoundOpt = refreshTokenRepository.findByToken(token);

      assertThat(tokenFoundOpt).isPresent();
      final RefreshTokenEntity tokenFound = tokenFoundOpt.get();
      assertThat(tokenFound.id()).isEqualTo(id);
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.family()).isEqualTo(family);
    }

    @Test
    void returns_empty_when_missing() {
      final Optional<RefreshTokenEntity> tokenFoundOpt =
          refreshTokenRepository.findByToken(RefreshToken.generate());

      assertThat(tokenFoundOpt).isEmpty();
    }
  }

  @Nested
  class UpdateByToken {

    @Test
    void updates_row_and_returns_true_when_it_succeeds() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final UUID id = UUID.randomUUID();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertRefreshToken(id, userId, token, now, expiration, family);

      final RefreshToken newToken = RefreshToken.generate();
      final UpdateRefreshTokenDto dto = UpdateRefreshTokenDto.builder().token(newToken).build();

      final boolean updated = refreshTokenRepository.updateByToken(token, dto);

      assertThat(updated).isTrue();
      final RefreshTokenEntity tokenFound =
          refreshTokenRepository.findByToken(newToken).orElseThrow();
      assertThat(tokenFound.id()).isEqualTo(id);
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.family()).isEqualTo(family);
    }

    @Test
    void does_nothing_and_returns_false_when_dto_is_all_null() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final UUID id = UUID.randomUUID();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertRefreshToken(id, userId, token, now, expiration, family);

      final UpdateRefreshTokenDto nullDto = UpdateRefreshTokenDto.builder().build();

      final boolean updated = refreshTokenRepository.updateByToken(token, nullDto);

      assertThat(updated).isTrue();
      final RefreshTokenEntity tokenFound = refreshTokenRepository.findByToken(token).orElseThrow();
      assertThat(tokenFound.id()).isEqualTo(id);
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.family()).isEqualTo(family);
    }

    @Test
    void does_nothing_and_returns_false_when_token_does_not_exist() {
      final ExpirationTime newExpiration = ExpirationTime.of(Instant.now().plusSeconds(2000));
      final UpdateRefreshTokenDto update =
          UpdateRefreshTokenDto.builder().expiresAt(newExpiration).build();

      final boolean updated = refreshTokenRepository.updateByToken(RefreshToken.generate(), update);

      assertThat(updated).isFalse();
    }
  }

  @Nested
  class UpdateByUserId {

    @Test
    void updates_row_and_returns_true_when_it_succeeds() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final UUID id = UUID.randomUUID();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertRefreshToken(id, userId, token, now, expiration, family);

      final RefreshToken newToken = RefreshToken.generate();
      final UpdateRefreshTokenDto dto = UpdateRefreshTokenDto.builder().token(newToken).build();

      final boolean updated = refreshTokenRepository.updateByUserId(userId, dto);

      assertThat(updated).isTrue();
      final RefreshTokenEntity tokenFound =
          refreshTokenRepository.findByToken(newToken).orElseThrow();
      assertThat(tokenFound.id()).isEqualTo(id);
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.family()).isEqualTo(family);
    }

    @Test
    void does_nothing_and_returns_false_when_dto_is_all_null() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final UUID id = UUID.randomUUID();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertRefreshToken(id, userId, token, now, expiration, family);

      final UpdateRefreshTokenDto nullDto = UpdateRefreshTokenDto.builder().build();

      final boolean updated = refreshTokenRepository.updateByUserId(userId, nullDto);

      assertThat(updated).isTrue();
      final RefreshTokenEntity tokenFound = refreshTokenRepository.findByToken(token).orElseThrow();
      assertThat(tokenFound.id()).isEqualTo(id);
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.family()).isEqualTo(family);
    }

    @Test
    void does_nothing_and_returns_false_when_user_does_not_exist() {
      final ExpirationTime newExpiration = ExpirationTime.of(Instant.now().plusSeconds(2000));
      final UpdateRefreshTokenDto update =
          UpdateRefreshTokenDto.builder().expiresAt(newExpiration).build();

      final boolean updated = refreshTokenRepository.updateByUserId(UserId.generate(), update);

      assertThat(updated).isFalse();
    }
  }

  @Nested
  class DeleteByToken {

    @Test
    void removes_row_and_returns_true_when_token_exists() {
      final UserId userId = insertRandomUser();
      final RefreshToken token = RefreshToken.generate();
      final TokenFamily family = TokenFamily.generate();
      final UUID id = UUID.randomUUID();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertRefreshToken(id, userId, token, now, expiration, family);

      final boolean deleted = refreshTokenRepository.deleteByToken(token);

      assertThat(deleted).isTrue();
      assertThat(refreshTokenRepository.findByToken(token)).isEmpty();
    }

    @Test
    void does_nothing_and_returns_false_when_token_does_not_exist() {
      final boolean deleted = refreshTokenRepository.deleteByToken(RefreshToken.generate());

      assertThat(deleted).isFalse();
    }
  }
}
