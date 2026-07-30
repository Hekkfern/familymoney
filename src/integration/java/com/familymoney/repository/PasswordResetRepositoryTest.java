package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.auth.repositories.PasswordResetRepository;
import com.familymoney.domains.auth.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
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
class PasswordResetRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private PasswordResetRepository passwordResetRepository;

  @BeforeEach
  void setUp() {
    this.passwordResetRepository = new PasswordResetRepository(dslContext);
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

  // region IPasswordResetRepository.create()

  @Test
  void create_persists_password_reset_token_record() {
    val userId = insertRandomUser();
    val token = PasswordResetToken.generate();
    val now = Instant.now();
    val expiresAt = ExpirationTime.of(now.plusSeconds(3600));

    val passwordResetOpt =
        passwordResetRepository.create(new CreatePasswordResetDto(userId, token, expiresAt));

    assertThat(passwordResetOpt).isPresent();
    val passwordReset = passwordResetOpt.get();
    assertThat(passwordReset.userId()).isNotNull().isEqualTo(userId);
    assertThat(passwordReset.token()).isNotNull().isEqualTo(token);
    assertThat(passwordReset.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(passwordReset.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(passwordReset.expiresAt().value())
        .isNotNull()
        .isBetween(expiresAt.value().minusSeconds(1), expiresAt.value().plusSeconds(1));
  }

  @Test
  void create_throws_when_user_does_not_exist() {
    val missingUserId = UserId.fromUuid(UUID.randomUUID());

    val dto =
        new CreatePasswordResetDto(
            missingUserId, PasswordResetToken.generate(), ExpirationTime.of(Instant.now().plusSeconds(300)));
    assertThatThrownBy(() -> passwordResetRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId = insertRandomUser();
    val token = PasswordResetToken.generate();
    val now = Instant.now();

    DatabaseCrud.insertPasswordResetToken(
        dslContext, userId, token, now, ExpirationTime.of(now.plusSeconds(300)));

    val dto2 = new CreatePasswordResetDto(userId, token, ExpirationTime.of(now.plusSeconds(600)));
    assertThatThrownBy(() -> passwordResetRepository.create(dto2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_user_already_has_an_entry() {
    val userId = insertRandomUser();
    val now = Instant.now();
    DatabaseCrud.insertPasswordResetToken(
        dslContext, userId, PasswordResetToken.generate(), now, ExpirationTime.of(now.plusSeconds(3600)));

    val dto =
        new CreatePasswordResetDto(userId, PasswordResetToken.generate(), ExpirationTime.of(now.plusSeconds(300)));
    assertThatThrownBy(() -> passwordResetRepository.create(dto))
        .isInstanceOf(DuplicateKeyException.class);
  }

  // endregion

  // region IPasswordResetRepository.findByToken()

  @Test
  void findByToken_returns_token_when_exists() {
    val userId = insertRandomUser();
    val token = PasswordResetToken.generate();
    val now = Instant.now();
    val expiration = ExpirationTime.of(now.plusSeconds(3600));
    DatabaseCrud.insertPasswordResetToken(dslContext, userId, token, now, expiration);

    val entryFoundOpt = passwordResetRepository.findByToken(token);

    assertThat(entryFoundOpt).isPresent();
    val entryFound = entryFoundOpt.get();
    assertThat(entryFound.userId()).isEqualTo(userId);
    assertThat(entryFound.token()).isEqualTo(token);
    assertThat(entryFound.expiresAt()).isEqualTo(expiration);
    assertThat(entryFound.createdAt()).isEqualTo(now);
    assertThat(entryFound.updatedAt()).isEqualTo(now);
  }

  @Test
  void findByToken_returns_empty_when_it_does_not_exist() {
    val entryFoundOpt = passwordResetRepository.findByToken(PasswordResetToken.generate());

    assertThat(entryFoundOpt).isEmpty();
  }

  // region

  // region IPasswordResetRepository.deleteByUserId()

  @Test
  void deleteByUserId_deletes_tokens() {
    val userId = insertRandomUser();
    val token = PasswordResetToken.generate();
    val now = Instant.now();
    DatabaseCrud.insertPasswordResetToken(dslContext, userId, token, now, ExpirationTime.of(now.plusSeconds(3600)));

    val deleted = passwordResetRepository.deleteByUserId(userId);

    assertThat(deleted).isTrue();
    assertThat(passwordResetRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByUserId_returns_false_when_missing() {
    val missingUserId = UserId.fromUuid(UUID.randomUUID());

    val deleted = passwordResetRepository.deleteByUserId(missingUserId);

    assertThat(deleted).isFalse();
  }

  // endregion
}
