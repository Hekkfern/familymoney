package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.auth.repositories.EmailVerificationRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.testutils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
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
class EmailVerificationRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private EmailVerificationRepository emailVerificationRepository;

  @BeforeEach
  void setUp() {
    this.emailVerificationRepository = new EmailVerificationRepository(dslContext);
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

  // region IEmailVerificationRepository.create()

  @Test
  void create_persists_email_verification_token_record() {
    val userId = insertRandomUser();
    val token = EmailVerificationToken.generate();
    val expiresAt = Instant.now().plusSeconds(3600);

    val tokenCreatedOpt =
        emailVerificationRepository.create(
            new CreateEmailVerificationDto(userId, token, expiresAt));

    assertThat(tokenCreatedOpt).isPresent();
    val tokenCreated = tokenCreatedOpt.get();
    assertThat(tokenCreated.userId()).isEqualTo(userId);
    assertThat(tokenCreated.token()).isEqualTo(token);
    assertThat(tokenCreated.expiresAt()).isEqualTo(expiresAt);
    assertThat(tokenCreated.createdAt()).isNotNull();
  }

  @Test
  void create_throws_when_user_does_not_exist() {
    val dto =
        new CreateEmailVerificationDto(
            UserId.generate(), EmailVerificationToken.generate(), Instant.now().plusSeconds(300));
    assertThatThrownBy(() -> emailVerificationRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_user_id_is_duplicate() {
    val userId = insertRandomUser();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);

    DatabaseCrud.insertEmailVerificationToken(
        dslContext, userId, EmailVerificationToken.generate(), now, expiration);

    val dto = new CreateEmailVerificationDto(userId, EmailVerificationToken.generate(), expiration);
    assertThatThrownBy(() -> emailVerificationRepository.create(dto))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val token = EmailVerificationToken.generate();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);

    DatabaseCrud.insertEmailVerificationToken(dslContext, userId1, token, now, expiration);

    val dto = new CreateEmailVerificationDto(userId2, token, expiration);
    assertThatThrownBy(() -> emailVerificationRepository.create(dto))
        .isInstanceOf(DuplicateKeyException.class);
  }

  // endregion

  // region IEmailVerificationRepository.findByUserId()

    @Test
    void findByUserId_returns_token_when_it_exists() {
      val userId = insertRandomUser();
      val token = EmailVerificationToken.generate();
      val now = Instant.now();
      val expiration = now.plusSeconds(3600);
      DatabaseCrud.insertEmailVerificationToken(dslContext, userId, token, now, expiration);

      val tokenFoundOpt = emailVerificationRepository.findByUserId(userId);

      assertThat(tokenFoundOpt).isPresent();
      val tokenFound = tokenFoundOpt.get();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.token()).isEqualTo(token);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.updatedAt()).isEqualTo(now);
    }

    @Test
    void findByUserId_returns_empty_when_it_does_not_exist() {
      val found = emailVerificationRepository.findByUserId(UserId.generate());

      assertThat(found).isEmpty();
    }

  // endregion

  // region IEmailVerificationRepository.findByToken()

  @Test
  void findByToken_returns_token_when_it_exists() {
    val userId = insertRandomUser();
    val token = EmailVerificationToken.generate();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertEmailVerificationToken(dslContext, userId, token, now, expiration);

    val tokenFoundOpt = emailVerificationRepository.findByToken(token);

    assertThat(tokenFoundOpt).isPresent();
    val tokenFound = tokenFoundOpt.get();
    assertThat(tokenFound.userId()).isEqualTo(userId);
    assertThat(tokenFound.token()).isEqualTo(token);
    assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
    assertThat(tokenFound.createdAt()).isEqualTo(now);
    assertThat(tokenFound.updatedAt()).isEqualTo(now);
  }

  @Test
  void findByToken_returns_empty_when_it_does_not_exist() {
    val found = emailVerificationRepository.findByToken(EmailVerificationToken.generate());

    assertThat(found).isEmpty();
  }

  // endregion

  // region IEmailVerificationRepository.updateByUserId()

    @Test
    void updateByUserId_updates_row_and_returns_true_when_it_succeeds() {
      val userId = insertRandomUser();
      val token = EmailVerificationToken.generate();
      val now = Instant.now();
      val expiration = now.plusSeconds(3600);
      DatabaseCrud.insertEmailVerificationToken(dslContext, userId, token, now, expiration);

      val newToken = EmailVerificationToken.generate();
      val dto = UpdateEmailVerificationTokenDto.builder().token(newToken).build();

      val updated = emailVerificationRepository.updateByUserId(userId, dto);

      assertThat(updated).isTrue();
      val tokenFound = emailVerificationRepository.findByUserId(userId).orElseThrow();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.token()).isEqualTo(newToken);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
    }

    @Test
    void updateByUserId_does_nothing_and_returns_false_when_dto_is_all_null() {
      val userId = insertRandomUser();
      val token = EmailVerificationToken.generate();
      val now = Instant.now();
      val expiration = now.plusSeconds(3600);
      DatabaseCrud.insertEmailVerificationToken(dslContext, userId, token, now, expiration);

      val nullDto = UpdateEmailVerificationTokenDto.builder().build();

      val updated = emailVerificationRepository.updateByUserId(userId, nullDto);

      assertThat(updated).isTrue();
      val tokenFound = emailVerificationRepository.findByUserId(userId).orElseThrow();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.token()).isEqualTo(token);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
    }

    @Test
    void updateByUserId_does_nothing_and_returns_false_when_user_does_not_exist() {
      val update = UpdateEmailVerificationTokenDto.builder()
          .token(EmailVerificationToken.generate())
          .build();

      val updated = emailVerificationRepository.updateByUserId(UserId.generate(), update);

      assertThat(updated).isFalse();
    }

  // endregion

  // region IEmailVerificationRepository.deleteByUserId()

  @Test
  void deleteByUserId_removes_tokens() {
    val userId = insertRandomUser();
    val token = EmailVerificationToken.generate();
    val now = Instant.now();
    val expiration = now.plusSeconds(3600);
    DatabaseCrud.insertEmailVerificationToken(dslContext, userId, token, now, expiration);

    val deleted = emailVerificationRepository.deleteByUserId(userId);

    assertThat(deleted).isTrue();
    assertThat(emailVerificationRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByUserId_returns_false_when_missing() {
    val deleted = emailVerificationRepository.deleteByUserId(UserId.generate());

    assertThat(deleted).isFalse();
  }

  // endregion
}
