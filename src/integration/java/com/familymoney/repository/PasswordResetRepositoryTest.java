package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.domains.auth.repositories.DefaultPasswordResetRepository;
import com.familymoney.domains.auth.repositories.PasswordResetRepository;
import com.familymoney.domains.auth.repositories.dtos.CreatePasswordResetDto;
import com.familymoney.domains.auth.repositories.dtos.UpdatePasswordResetDto;
import com.familymoney.domains.auth.repositories.entitites.PasswordResetEntity;
import com.familymoney.domains.auth.types.ExpirationTime;
import com.familymoney.domains.auth.types.PasswordResetToken;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.generated.tables.PasswordResetTokens;
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
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    passwordResetRepository =
        new DefaultPasswordResetRepository(dslContext, new DefaultOpaqueTokenHasher());
    databaseCrud = new DatabaseCrud(dslContext);
  }

  @Nested
  class Create {

    @Test
    void persists_a_hashed_password_reset_token() {
      final UserId userId = insertRandomUser();
      final PasswordResetToken token = PasswordResetToken.generate();
      final Instant now = Instant.now();
      final ExpirationTime expiresAt = ExpirationTime.of(now.plusSeconds(3600));

      final Optional<PasswordResetEntity> result =
          passwordResetRepository.create(new CreatePasswordResetDto(userId, token, expiresAt, now));

      assertThat(result).isPresent();
      assertThat(result.get().userId()).isEqualTo(userId);
      assertThat(result.get().expiresAt()).isEqualTo(expiresAt);
      assertThat(result.get().lastSentAt()).isEqualTo(now);
      assertThat(
              dslContext
                  .select(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH)
                  .from(PasswordResetTokens.PASSWORD_RESET_TOKENS)
                  .fetchSingle(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN_HASH))
          .isEqualTo(new DefaultOpaqueTokenHasher().hash(token.value()))
          .isNotEqualTo(token.value());
    }
  }

  @Nested
  class Find {

    @Test
    void finds_a_token_by_user_id_and_token() {
      final UserId userId = insertRandomUser();
      final PasswordResetToken token = PasswordResetToken.generate();
      final Instant now = Instant.now();
      passwordResetRepository.create(
          new CreatePasswordResetDto(userId, token, ExpirationTime.of(now.plusSeconds(3600)), now));

      assertThat(passwordResetRepository.findByUserId(userId)).isPresent();
      assertThat(passwordResetRepository.findByToken(token)).isPresent();
      assertThat(passwordResetRepository.findByToken(PasswordResetToken.generate())).isEmpty();
    }
  }

  @Nested
  class UpdateByUserId {

    @Test
    void replaces_the_token_values_for_the_user() {
      final UserId userId = insertRandomUser();
      final PasswordResetToken initialToken = PasswordResetToken.generate();
      final PasswordResetToken replacementToken = PasswordResetToken.generate();
      final Instant now = Instant.now();
      passwordResetRepository.create(
          new CreatePasswordResetDto(
              userId, initialToken, ExpirationTime.of(now.plusSeconds(300)), now));

      final boolean updated =
          passwordResetRepository.updateByUserId(
              userId,
              new UpdatePasswordResetDto(
                  replacementToken, ExpirationTime.of(now.plusSeconds(600)), now.plusSeconds(60)));

      assertThat(updated).isTrue();
      assertThat(passwordResetRepository.findByToken(initialToken)).isEmpty();
      assertThat(passwordResetRepository.findByToken(replacementToken)).isPresent();
    }
  }

  @Nested
  class DeleteByUserId {

    @Test
    void deletes_the_password_reset_token() {
      final UserId userId = insertRandomUser();
      final PasswordResetToken token = PasswordResetToken.generate();
      final Instant now = Instant.now();
      passwordResetRepository.create(
          new CreatePasswordResetDto(userId, token, ExpirationTime.of(now.plusSeconds(3600)), now));

      assertThat(passwordResetRepository.deleteByUserId(userId)).isTrue();
      assertThat(passwordResetRepository.findByToken(token)).isEmpty();
    }
  }

  private UserId insertRandomUser() {
    final UserId userId = UserId.generate();
    databaseCrud.insertUser(
        userId,
        UserName.fromString(FakeGenerator.username()),
        Email.fromString(FakeGenerator.email()),
        "hashed_password",
        Instant.ofEpochSecond(1778755330),
        true,
        true);
    return userId;
  }
}
