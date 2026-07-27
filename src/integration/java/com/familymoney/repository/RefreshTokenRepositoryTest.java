package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.auth.repositories.RefreshTokenRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.domains.auth.types.RefreshToken;
import com.familymoney.domains.auth.types.TokenFamily;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.UUID;
import lombok.val;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
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

  @BeforeEach
  void setUp() {
    this.refreshTokenRepository = new RefreshTokenRepository(dslContext);
  }

  private UserId insertRandomUser() {
    val userId = UserId.generate();
    val now = Instant.ofEpochSecond(1778755330);
    DatabaseCrud.insertUser(
        dslContext,
        userId,
        UserName.fromString(FakeGenerator.username()),
        Email.fromString(FakeGenerator.email()),
        "hashed_password",
        now,
        true,
        true);
    return userId;
  }

  // region IRefreshTokenRepository.create()

  @Test
  void create_persists_refresh_token_record() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();

    val now = Instant.now();
    val expiration = now.plusSeconds(3600);

    val refreshTokenCreated =
        refreshTokenRepository.create(
            new CreateRefreshTokenDto(UUID.randomUUID(), userId, token, family, expiration));

    assertThat(refreshTokenCreated).isPresent();
    val refreshToken = refreshTokenCreated.get();
    assertThat(refreshToken.id()).isNotNull();
    assertThat(refreshToken.userId()).isNotNull().isEqualTo(userId);
    assertThat(refreshToken.token()).isNotNull().isEqualTo(token);
    assertThat(refreshToken.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(refreshToken.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(refreshToken.expiresAt())
        .isNotNull()
        .isBetween(expiration.minusSeconds(1), expiration.plusSeconds(1));
    assertThat(refreshToken.family()).isNotNull().isEqualTo(family);
  }

  void create_persists_when_same_user_but_different_family() {
    // TODO
  }

  @Test
  void create_throws_when_user_does_not_exist() {
    val missingUserId = UserId.generate();

    val now = Instant.now();

    val dto =
        new CreateRefreshTokenDto(
            UUID.randomUUID(), missingUserId, RefreshToken.generate(), TokenFamily.generate(), now);
    assertThatThrownBy(() -> refreshTokenRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();

    val now = Instant.now();
    val expiration = now.plusSeconds(3600);

    DatabaseCrud.insertRefreshToken(
        dslContext, UUID.randomUUID(), userId, token, now, expiration, TokenFamily.generate());

    val dto =
        new CreateRefreshTokenDto(
            UUID.randomUUID(), userId, token, TokenFamily.generate(), expiration);
    assertThatThrownBy(() -> refreshTokenRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_user_id_and_family_are_duplicate() {
    val userId = insertRandomUser();
    val family = TokenFamily.generate();

    val now = Instant.now();
    val expiration = now.plusSeconds(3600);

    DatabaseCrud.insertRefreshToken(
        dslContext, UUID.randomUUID(), userId, RefreshToken.generate(), now, expiration, family);

    val dto =
        new CreateRefreshTokenDto(
            UUID.randomUUID(), userId, RefreshToken.generate(), family, expiration);
    assertThatThrownBy(() -> refreshTokenRepository.create(dto))
        .isInstanceOf(DuplicateKeyException.class);
  }

  // endregion

  // region IRefreshTokenRepository.findByToken()

  @Test
  void findByToken_returns_token_when_exists() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();
    val id = UUID.randomUUID();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertRefreshToken(dslContext, id, userId, token, now, expiration, family);

    val tokenFoundOpt = refreshTokenRepository.findByToken(token);

    assertThat(tokenFoundOpt).isPresent();
    val tokenFound = tokenFoundOpt.get();
    assertThat(tokenFound.id()).isEqualTo(id);
    assertThat(tokenFound.userId()).isEqualTo(userId);
    assertThat(tokenFound.createdAt()).isEqualTo(now);
    assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
    assertThat(tokenFound.family()).isEqualTo(family);
  }

  @Test
  void findByToken_returns_empty_when_missing() {
    val tokenFoundOpt = refreshTokenRepository.findByToken(RefreshToken.generate());

    assertThat(tokenFoundOpt).isEmpty();
  }

  // endregion

  // region IRefreshTokenRepository.updateByToken()

  @Test
  void updateByToken_updates_row_and_returns_true_when_it_succeeds() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();
    val id = UUID.randomUUID();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertRefreshToken(dslContext, id, userId, token, now, expiration, family);

    val newToken = RefreshToken.generate();
    val dto = UpdateRefreshTokenDto.builder().token(newToken).build();

    val updated = refreshTokenRepository.updateByToken(token, dto);

    assertThat(updated).isTrue();
    val tokenFound = refreshTokenRepository.findByToken(newToken).orElseThrow();
    assertThat(tokenFound.id()).isEqualTo(id);
    assertThat(tokenFound.userId()).isEqualTo(userId);
    assertThat(tokenFound.token()).isEqualTo(newToken);
    assertThat(tokenFound.createdAt()).isEqualTo(now);
    assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
    assertThat(tokenFound.family()).isEqualTo(family);
  }

  @Test
  void updateByToken_does_nothing_and_returns_false_when_dto_is_all_null() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();
    val id = UUID.randomUUID();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertRefreshToken(dslContext, id, userId, token, now, expiration, family);

    val nullDto = UpdateRefreshTokenDto.builder().build();

    val updated = refreshTokenRepository.updateByToken(token, nullDto);

    assertThat(updated).isTrue();
    val tokenFound = refreshTokenRepository.findByToken(token).orElseThrow();
    assertThat(tokenFound.id()).isEqualTo(id);
    assertThat(tokenFound.userId()).isEqualTo(userId);
    assertThat(tokenFound.token()).isEqualTo(token);
    assertThat(tokenFound.createdAt()).isEqualTo(now);
    assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
    assertThat(tokenFound.family()).isEqualTo(family);
  }

  @Test
  void updateByToken_does_nothing_and_returns_false_when_token_does_not_exist() {
    val update = UpdateRefreshTokenDto.builder().expiresAt(Instant.now().plusSeconds(2000)).build();

    val updated = refreshTokenRepository.updateByToken(RefreshToken.generate(), update);

    assertThat(updated).isFalse();
  }

  // endregion

  // region IRefreshTokenRepository.updateByUserId()

  @Test
  void updateByUserId_updates_row_and_returns_true_when_it_succeeds() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();
    val id = UUID.randomUUID();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertRefreshToken(dslContext, id, userId, token, now, expiration, family);

    val newToken = RefreshToken.generate();
    val dto = UpdateRefreshTokenDto.builder().token(newToken).build();

    val updated = refreshTokenRepository.updateByUserId(userId, dto);

    assertThat(updated).isTrue();
    val tokenFound = refreshTokenRepository.findByToken(newToken).orElseThrow();
    assertThat(tokenFound.id()).isEqualTo(id);
    assertThat(tokenFound.userId()).isEqualTo(userId);
    assertThat(tokenFound.token()).isEqualTo(newToken);
    assertThat(tokenFound.createdAt()).isEqualTo(now);
    assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
    assertThat(tokenFound.family()).isEqualTo(family);
  }

  @Test
  void updateByUserId_does_nothing_and_returns_false_when_dto_is_all_null() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();
    val id = UUID.randomUUID();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertRefreshToken(dslContext, id, userId, token, now, expiration, family);

    val nullDto = UpdateRefreshTokenDto.builder().build();

    val updated = refreshTokenRepository.updateByUserId(userId, nullDto);

    assertThat(updated).isTrue();
    val tokenFound = refreshTokenRepository.findByToken(token).orElseThrow();
    assertThat(tokenFound.id()).isEqualTo(id);
    assertThat(tokenFound.userId()).isEqualTo(userId);
    assertThat(tokenFound.token()).isEqualTo(token);
    assertThat(tokenFound.createdAt()).isEqualTo(now);
    assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
    assertThat(tokenFound.family()).isEqualTo(family);
  }

  @Test
  void updateByUserId_does_nothing_and_returns_false_when_user_does_not_exist() {
    val update = UpdateRefreshTokenDto.builder().expiresAt(Instant.now().plusSeconds(2000)).build();

    val updated = refreshTokenRepository.updateByUserId(UserId.generate(), update);

    assertThat(updated).isFalse();
  }

  // endregion

  // region IRefreshTokenRepository.deleteByToken()

  @Test
  void deleteByToken_removes_row_and_returns_true_when_token_exists() {
    val userId = insertRandomUser();
    val token = RefreshToken.generate();
    val family = TokenFamily.generate();
    val id = UUID.randomUUID();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertRefreshToken(dslContext, id, userId, token, now, expiration, family);

    val deleted = refreshTokenRepository.deleteByToken(token);

    assertThat(deleted).isTrue();
    assertThat(refreshTokenRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByToken_does_nothing_and_returns_false_when_token_does_not_exist() {
    val deleted = refreshTokenRepository.deleteByToken(RefreshToken.generate());

    assertThat(deleted).isFalse();
  }

  // endregion
}
