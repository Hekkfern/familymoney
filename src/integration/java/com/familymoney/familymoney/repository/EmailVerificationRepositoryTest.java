package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.familymoney.repositories.impl.EmailVerificationRepository;
import com.familymoney.familymoney.repositories.entities.EmailVerificationEntity;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.utils.FakeGenerator;

import java.time.Instant;
import java.time.OffsetDateTime;

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
public class EmailVerificationRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private EmailVerificationRepository emailVerificationRepository;

  @BeforeEach
  void setUp() {
    this.emailVerificationRepository = new EmailVerificationRepository(dslContext);
  }

  private UserId insertUser(String username, String email) {
    val record =
        dslContext
            .insertInto(Users.USERS)
            .columns(Users.USERS.USERNAME, Users.USERS.EMAIL, Users.USERS.HASHED_PASSWORD)
            .values(username, email, "hashed-password")
            .returning(Users.USERS.ID)
            .fetchOne();
    return UserId.fromUuid(record.getId());
  }

  private EmailVerificationEntity insertToken(
      UserId userId, EmailVerificationToken token, OffsetDateTime createdAt) {
    val record =
        dslContext
            .insertInto(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
            .columns(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT)
            .values(userId.value(), token.value(), createdAt.plusDays(1), createdAt)
            .returning(
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.ID,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.USER_ID,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.EXPIRES_AT,
                EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.CREATED_AT)
            .fetchOne();
    return EmailVerificationEntity.builder()
        .id(record.getId())
        .userId(UserId.fromUuid(record.getUserId()))
        .token(EmailVerificationToken.fromString(record.getToken()))
        .expiresAt(record.getExpiresAt().toInstant())
        .createdAt(record.getCreatedAt().toInstant())
        .build();
  }

  @Test
  void create_persists_email_verification_token() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = EmailVerificationToken.generate();
    val expiresAt = Instant.now().plusSeconds(3600);

    val created =
        emailVerificationRepository.create(
            new CreateEmailVerificationDto(any(), userId, token, expiresAt));

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.userId()).isEqualTo(userId);
    assertThat(dbo.token()).isEqualTo(token);
    assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
    assertThat(dbo.createdAt()).isNotNull();
  }

  @Test
  void create_throws_when_user_missing() {
    val missingUserId = UserId.fromUuid(java.util.UUID.randomUUID());

    assertThatThrownBy(
            () ->
                emailVerificationRepository.create(
                    new CreateEmailVerificationDto(
                        any(),
                        missingUserId,
                        EmailVerificationToken.generate(),
                        Instant.now().plusSeconds(300))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = EmailVerificationToken.generate();

    emailVerificationRepository.create(
        new CreateEmailVerificationDto(any(), userId, token, Instant.now().plusSeconds(300)));

    assertThatThrownBy(
            () ->
                emailVerificationRepository.create(
                    new CreateEmailVerificationDto(
                        any(), userId, token, Instant.now().plusSeconds(600))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findByToken_returns_token_when_exists() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = EmailVerificationToken.generate();
    emailVerificationRepository.create(
        new CreateEmailVerificationDto(any(), userId, token, Instant.now().plusSeconds(300)));

    val found = emailVerificationRepository.findByToken(token);

    assertThat(found).isPresent();
    assertThat(found.get().userId()).isEqualTo(userId);
    assertThat(found.get().token()).isEqualTo(token);
  }

  @Test
  void findByToken_returns_empty_when_missing() {
    val found = emailVerificationRepository.findByToken(EmailVerificationToken.generate());

    assertThat(found).isEmpty();
  }

  @Test
  void deleteByUserId_removes_tokens() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val token = EmailVerificationToken.generate();
    emailVerificationRepository.create(new CreateEmailVerificationDto(any(), userId, token, Instant.now().plusSeconds(300)));

    val deleted = emailVerificationRepository.deleteByUserId(userId);

    assertThat(deleted).isTrue();
    assertThat(emailVerificationRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByUserId_returns_false_when_missing() {
    val missingUserId = UserId.fromUuid(java.util.UUID.randomUUID());

    val deleted = emailVerificationRepository.deleteByUserId(missingUserId);

    assertThat(deleted).isFalse();
  }
}
