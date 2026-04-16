package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.familymoney.generated.tables.RefreshTokens;
import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.dtos.CreateRefreshTokenDto;
import com.familymoney.familymoney.repositories.dtos.UpdateRefreshTokenDto;
import com.familymoney.familymoney.repositories.impl.RefreshTokenRepository;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.val;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class RefreshTokenRepositoryTests {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void setUp() {
    this.refreshTokenRepository = new RefreshTokenRepository(dslContext);
  }

  private UserId insertUser(final String username, final String email) {
    val record =
        dslContext
            .insertInto(Users.USERS)
            .columns(Users.USERS.USERNAME, Users.USERS.EMAIL, Users.USERS.HASHED_PASSWORD)
            .values(username, email, "hashed-password")
            .returning(Users.USERS.ID)
            .fetchOne();
    return UserId.fromUuid(record.getId());
  }

  private void insertToken(
      final UserId userId,
      final RefreshToken token,
      final UUID family,
      final OffsetDateTime createdAt,
      final boolean isUsed,
      @Nullable final OffsetDateTime usedAt) {
    dslContext
        .insertInto(RefreshTokens.REFRESH_TOKENS)
        .columns(
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.IS_USED,
            RefreshTokens.REFRESH_TOKENS.USED_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .values(
            userId.value(), token.value(), createdAt, createdAt.plusDays(7), isUsed, usedAt, family)
        .returning(
            RefreshTokens.REFRESH_TOKENS.ID,
            RefreshTokens.REFRESH_TOKENS.USER_ID,
            RefreshTokens.REFRESH_TOKENS.TOKEN,
            RefreshTokens.REFRESH_TOKENS.CREATED_AT,
            RefreshTokens.REFRESH_TOKENS.EXPIRES_AT,
            RefreshTokens.REFRESH_TOKENS.IS_USED,
            RefreshTokens.REFRESH_TOKENS.USED_AT,
            RefreshTokens.REFRESH_TOKENS.FAMILY)
        .execute();
  }

  @Test
  void create_persists_refresh_token() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = RefreshToken.generate();
    val family = UUID.randomUUID();

    val created =
        refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, token, family));

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.userId()).isEqualTo(userId);
    assertThat(dbo.token()).isEqualTo(token);
    assertThat(dbo.family()).isEqualTo(family);
    assertThat(dbo.isUsed()).isFalse();
    assertThat(dbo.usedAt()).isEmpty();
  }

  @Test
  void create_throws_when_user_missing() {
    val missingUserId = UserId.fromUuid(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                refreshTokenRepository.create(
                    new CreateRefreshTokenDto(
                        any(), missingUserId, RefreshToken.generate(), UUID.randomUUID())))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = RefreshToken.generate();
    val family = UUID.randomUUID();

    refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, token, family));

    assertThatThrownBy(
            () ->
                refreshTokenRepository.create(
                    new CreateRefreshTokenDto(any(), userId, token, UUID.randomUUID())))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findByToken_returns_token_when_exists() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = RefreshToken.generate();
    val family = UUID.randomUUID();
    refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, token, family));

    val found = refreshTokenRepository.findByToken(token);

    assertThat(found).isPresent();
    val tokenDb = found.get();
    assertThat(tokenDb.userId()).isEqualTo(userId);
    assertThat(tokenDb.token()).isEqualTo(token);
  }

  @Test
  void findByToken_returns_empty_when_missing() {
    val found = refreshTokenRepository.findByToken(RefreshToken.generate());

    assertThat(found).isEmpty();
  }

  @Test
  void updateByToken_updates_row_and_returns_true() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = RefreshToken.generate();
    val family = UUID.randomUUID();
    refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, token, family));
    val update = UpdateRefreshTokenDto.builder().isUsed(true).usedAt(Instant.now()).build();

    val updated = refreshTokenRepository.updateByToken(token, update);

    assertThat(updated).isTrue();
    val found = refreshTokenRepository.findByToken(token).orElseThrow();
    assertThat(found.isUsed()).isTrue();
    assertThat(found.usedAt()).isPresent();
  }

  @Test
  void updateByToken_returns_false_when_missing() {
    val update = UpdateRefreshTokenDto.builder().isUsed(true).build();

    val updated = refreshTokenRepository.updateByToken(RefreshToken.generate(), update);

    assertThat(updated).isFalse();
  }

  @Test
  void updateByFamily_updates_rows_and_returns_true() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val family = UUID.randomUUID();
    val tokenA = RefreshToken.generate();
    val tokenB = RefreshToken.generate();
    refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, tokenA, family));
    refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, tokenB, family));
    val update = UpdateRefreshTokenDto.builder().isUsed(true).usedAt(Instant.now()).build();

    val updated = refreshTokenRepository.updateByFamily(family, update);

    assertThat(updated).isTrue();
    assertThat(refreshTokenRepository.findByToken(tokenA)).isPresent();
    assertThat(refreshTokenRepository.findByToken(tokenA).orElseThrow().isUsed()).isTrue();
    assertThat(refreshTokenRepository.findByToken(tokenB).orElseThrow().isUsed()).isTrue();
  }

  @Test
  void updateByFamily_returns_false_when_missing() {
    val update = UpdateRefreshTokenDto.builder().isUsed(true).build();

    val updated = refreshTokenRepository.updateByFamily(UUID.randomUUID(), update);

    assertThat(updated).isFalse();
  }

  @Test
  void updateByUserId_updates_rows_and_returns_true() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val family = UUID.randomUUID();
    val token = RefreshToken.generate();
    refreshTokenRepository.create(new CreateRefreshTokenDto(any(), userId, token, family));
    val update = UpdateRefreshTokenDto.builder().isUsed(true).usedAt(Instant.now()).build();

    val updated = refreshTokenRepository.updateByUserId(userId, update);

    assertThat(updated).isTrue();
    assertThat(refreshTokenRepository.findByToken(token).orElseThrow().isUsed()).isTrue();
  }

  @Test
  void updateByUserId_returns_false_when_missing() {
    val update = UpdateRefreshTokenDto.builder().isUsed(true).build();

    val updated = refreshTokenRepository.updateByUserId(UserId.fromUuid(UUID.randomUUID()), update);

    assertThat(updated).isFalse();
  }
}
