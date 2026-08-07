package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.auth.repositories.EmailVerificationRepository;
import com.familymoney.domains.auth.repositories.dtos.CreateEmailVerificationDto;
import com.familymoney.domains.auth.repositories.dtos.UpdateEmailVerificationTokenDto;
import com.familymoney.domains.auth.repositories.entitites.EmailVerificationEntity;
import com.familymoney.domains.auth.types.EmailVerificationToken;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.EmailVerificationTokens;
import com.familymoney.security.DefaultOpaqueTokenHasher;
import com.familymoney.test_utils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
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
class EmailVerificationRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private EmailVerificationRepository emailVerificationRepository;
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    this.emailVerificationRepository =
        new EmailVerificationRepository(dslContext, new DefaultOpaqueTokenHasher());
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
    void persists_email_verification_token_record() {
      final UserId userId = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final ExpirationTime expiresAt = ExpirationTime.of(Instant.now().plusSeconds(3600));

      final Optional<EmailVerificationEntity> tokenCreatedOpt =
          emailVerificationRepository.create(
              new CreateEmailVerificationDto(userId, token, expiresAt));

      assertThat(tokenCreatedOpt).isPresent();
      final EmailVerificationEntity tokenCreated = tokenCreatedOpt.get();
      assertThat(tokenCreated.userId()).isEqualTo(userId);
      assertThat(tokenCreated.expiresAt()).isEqualTo(expiresAt);
      assertThat(tokenCreated.createdAt()).isNotNull();
      assertThat(tokenCreated.lastSentAt()).isNotNull();
      assertThat(
              dslContext
                  .select(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH)
                  .from(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS)
                  .fetchSingle(EmailVerificationTokens.EMAIL_VERIFICATION_TOKENS.TOKEN_HASH))
          .isEqualTo(new DefaultOpaqueTokenHasher().hash(token.value()))
          .isNotEqualTo(token.value());
    }

    @Test
    void throws_when_user_does_not_exist() {
      final CreateEmailVerificationDto dto =
          new CreateEmailVerificationDto(
              UserId.generate(),
              EmailVerificationToken.generate(),
              ExpirationTime.of(Instant.now().plusSeconds(300)));
      assertThatThrownBy(() -> emailVerificationRepository.create(dto))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throws_when_user_id_is_duplicate() {
      final UserId userId = insertRandomUser();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));

      databaseCrud.insertEmailVerificationToken(
          userId, EmailVerificationToken.generate(), now, expiration);

      final CreateEmailVerificationDto dto =
          new CreateEmailVerificationDto(userId, EmailVerificationToken.generate(), expiration);
      assertThatThrownBy(() -> emailVerificationRepository.create(dto))
          .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void throws_when_token_is_duplicate() {
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));

      databaseCrud.insertEmailVerificationToken(userId1, token, now, expiration);

      final CreateEmailVerificationDto dto =
          new CreateEmailVerificationDto(userId2, token, expiration);
      assertThatThrownBy(() -> emailVerificationRepository.create(dto))
          .isInstanceOf(DuplicateKeyException.class);
    }
  }

  @Nested
  class FindByUserId {

    @Test
    void returns_token_when_it_exists() {
      final UserId userId = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertEmailVerificationToken(userId, token, now, expiration);

      final Optional<EmailVerificationEntity> tokenFoundOpt =
          emailVerificationRepository.findByUserId(userId);

      assertThat(tokenFoundOpt).isPresent();
      final EmailVerificationEntity tokenFound = tokenFoundOpt.get();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.updatedAt()).isEqualTo(now);
      assertThat(tokenFound.lastSentAt()).isEqualTo(now);
    }

    @Test
    void returns_empty_when_it_does_not_exist() {
      final Optional<EmailVerificationEntity> found =
          emailVerificationRepository.findByUserId(UserId.generate());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class FindByToken {

    @Test
    void returns_token_when_it_exists() {
      final UserId userId = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertEmailVerificationToken(userId, token, now, expiration);

      final Optional<EmailVerificationEntity> tokenFoundOpt =
          emailVerificationRepository.findByToken(token);

      assertThat(tokenFoundOpt).isPresent();
      final EmailVerificationEntity tokenFound = tokenFoundOpt.get();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.updatedAt()).isEqualTo(now);
      assertThat(tokenFound.lastSentAt()).isEqualTo(now);
    }

    @Test
    void returns_empty_when_it_does_not_exist() {
      final Optional<EmailVerificationEntity> found =
          emailVerificationRepository.findByToken(EmailVerificationToken.generate());

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class UpdateByUserId {

    @Test
    void updates_row_and_returns_true_when_it_succeeds() {
      final UserId userId = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertEmailVerificationToken(userId, token, now, expiration);

      final EmailVerificationToken newToken = EmailVerificationToken.generate();
      final UpdateEmailVerificationTokenDto dto =
          UpdateEmailVerificationTokenDto.builder().token(newToken).build();

      final boolean updated = emailVerificationRepository.updateByUserId(userId, dto);

      assertThat(updated).isTrue();
      final EmailVerificationEntity tokenFound =
          emailVerificationRepository.findByUserId(userId).orElseThrow();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
      assertThat(tokenFound.lastSentAt()).isEqualTo(now);
    }

    @Test
    void does_nothing_and_returns_false_when_dto_is_all_null() {
      final UserId userId = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertEmailVerificationToken(userId, token, now, expiration);

      final UpdateEmailVerificationTokenDto nullDto =
          UpdateEmailVerificationTokenDto.builder().build();

      final boolean updated = emailVerificationRepository.updateByUserId(userId, nullDto);

      assertThat(updated).isTrue();
      final EmailVerificationEntity tokenFound =
          emailVerificationRepository.findByUserId(userId).orElseThrow();
      assertThat(tokenFound.userId()).isEqualTo(userId);
      assertThat(tokenFound.expiresAt()).isEqualTo(expiration);
      assertThat(tokenFound.createdAt()).isEqualTo(now);
    }

    @Test
    void does_nothing_and_returns_false_when_user_does_not_exist() {
      final UpdateEmailVerificationTokenDto update =
          UpdateEmailVerificationTokenDto.builder()
              .token(EmailVerificationToken.generate())
              .build();

      final boolean updated = emailVerificationRepository.updateByUserId(UserId.generate(), update);

      assertThat(updated).isFalse();
    }
  }

  @Nested
  class DeleteByUserId {

    @Test
    void removes_tokens() {
      final UserId userId = insertRandomUser();
      final EmailVerificationToken token = EmailVerificationToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));
      databaseCrud.insertEmailVerificationToken(userId, token, now, expiration);

      final boolean deleted = emailVerificationRepository.deleteByUserId(userId);

      assertThat(deleted).isTrue();
      assertThat(emailVerificationRepository.findByToken(token)).isEmpty();
    }

    @Test
    void returns_false_when_missing() {
      final boolean deleted = emailVerificationRepository.deleteByUserId(UserId.generate());

      assertThat(deleted).isFalse();
    }
  }
}
