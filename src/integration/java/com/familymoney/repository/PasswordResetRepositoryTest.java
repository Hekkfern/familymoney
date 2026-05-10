package com.familymoney.repository;

import static com.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.generated.tables.Users;
import com.familymoney.domains.auth.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.domains.auth.repositories.PasswordResetRepository;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.utils.FakeGenerator;
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

  private UserId insertUser(final String username, final String email) {
    val r =
        dslContext
            .insertInto(Users.USERS)
            .columns(Users.USERS.USERNAME, Users.USERS.EMAIL, Users.USERS.HASHED_PASSWORD)
            .values(username, email, "hashed-password")
            .returning(Users.USERS.ID)
            .fetchOne();
    return UserId.fromUuid(r.getId());
  }

  @Test
  void create_persists_password_reset_token() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = PasswordResetToken.generate();
    val expiresAt = Instant.now().plusSeconds(3600);

    val created =
        passwordResetRepository.create(new CreatePasswordResetDto(any(), userId, token, expiresAt));

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.userId()).isEqualTo(userId);
    assertThat(dbo.token()).isEqualTo(token);
    assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
  }

  @Test
  void create_throws_when_user_missing() {
    val missingUserId = UserId.fromUuid(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                passwordResetRepository.create(
                    new CreatePasswordResetDto(
                        any(),
                        missingUserId,
                        PasswordResetToken.generate(),
                        Instant.now().plusSeconds(300))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = PasswordResetToken.generate();

    passwordResetRepository.create(
        new CreatePasswordResetDto(any(), userId, token, Instant.now().plusSeconds(300)));

    assertThatThrownBy(
            () ->
                passwordResetRepository.create(
                    new CreatePasswordResetDto(
                        any(), userId, token, Instant.now().plusSeconds(600))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findByToken_returns_token_when_exists() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = PasswordResetToken.generate();
    passwordResetRepository.create(
        new CreatePasswordResetDto(any(), userId, token, Instant.now().plusSeconds(300)));

    val found = passwordResetRepository.findByToken(token);

    assertThat(found).isPresent();
    assertThat(found.get().userId()).isEqualTo(userId);
    assertThat(found.get().token()).isEqualTo(token);
  }

  @Test
  void findByToken_returns_empty_when_missing() {
    val found = passwordResetRepository.findByToken(PasswordResetToken.generate());

    assertThat(found).isEmpty();
  }

  @Test
  void deleteByUserId_deletes_tokens() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = PasswordResetToken.generate();
    passwordResetRepository.create(
        new CreatePasswordResetDto(any(), userId, token, Instant.now().plusSeconds(300)));

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
}
