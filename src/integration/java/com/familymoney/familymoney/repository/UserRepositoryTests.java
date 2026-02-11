package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.impl.UserRepository;
import com.familymoney.familymoney.repositories.dbos.UpdateUserDbo;
import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.val;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
public class UserRepositoryTests {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    this.userRepository = new UserRepository(dslContext);
  }

  private UserDbo createUser(final String username, final String email) {
    return userRepository
        .create(UserName.fromString(username), Email.fromString(email), "hashed-password")
        .orElseThrow();
  }

  private UserId insertUserWithFields(
      final String username,
      final String email,
      final OffsetDateTime createdAt,
      final boolean isEmailVerified,
      final boolean isEnabled) {
    val record =
        dslContext
            .insertInto(Users.USERS)
            .columns(
                Users.USERS.USERNAME,
                Users.USERS.EMAIL,
                Users.USERS.HASHED_PASSWORD,
                Users.USERS.CREATED_AT,
                Users.USERS.UPDATED_AT,
                Users.USERS.IS_EMAIL_VERIFIED,
                Users.USERS.IS_ENABLED)
            .values(
                username,
                email,
                "hashed-password",
                createdAt,
                createdAt,
                isEmailVerified,
                isEnabled)
            .returning(Users.USERS.ID)
            .fetchOne();
    return UserId.fromUuid(record.getId());
  }

  @Test
  void create_persists_user_record() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    val now = Instant.now();

    val userCreated = userRepository.create(username, email, passwordHash);

    assertThat(userCreated).isPresent();
    val user = userCreated.get();
    assertThat(user.id()).isNotNull();
    assertThat(user.username()).isEqualTo(username);
    assertThat(user.email()).isEqualTo(email);
    assertThat(user.hashedPassword()).isEqualTo(passwordHash);
    assertThat(user.createdAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(user.updatedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(user.isEnabled()).isTrue();
    assertThat(user.isEmailVerified()).isFalse();
  }

  @Test
  void create_throws_when_email_is_duplicate() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    userRepository.create(username, email, passwordHash);

    assertThatThrownBy(
            () ->
                userRepository.create(
                    UserName.fromString(FakeGenerator.username()), email, passwordHash))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void create_throws_when_username_is_duplicate() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    userRepository.create(username, email, passwordHash);

    assertThatThrownBy(
            () ->
                userRepository.create(username, Email.fromString(FakeGenerator.email()), passwordHash))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void findById_returns_user_when_exists() {
    val created = createUser(FakeGenerator.username(), FakeGenerator.email());

    val found = userRepository.findById(created.id());

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(created.id());
  }

  @Test
  void findById_returns_empty_when_missing() {
    val missing = UserId.fromUuid(java.util.UUID.randomUUID());

    val found = userRepository.findById(missing);

    assertThat(found).isEmpty();
  }

  @Test
  void findByEmail_returns_user_when_exists() {
    val email = FakeGenerator.email();
    val created = createUser(FakeGenerator.username(), email);

    val found = userRepository.findByEmail(Email.fromString(email));

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(created.id());
  }

  @Test
  void findByEmail_returns_empty_when_missing() {
    val found = userRepository.findByEmail(Email.fromString(FakeGenerator.email()));

    assertThat(found).isEmpty();
  }

  @Test
  void findByUsername_returns_user_when_exists() {
    val username = FakeGenerator.username();
    val created = createUser(username, FakeGenerator.email());

    val found = userRepository.findByUsername(UserName.fromString(username));

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(created.id());
  }

  @Test
  void findByUsername_returns_empty_when_missing() {
    val found = userRepository.findByUsername(UserName.fromString(FakeGenerator.username()));

    assertThat(found).isEmpty();
  }

  @Test
  void existsByEmailOrUsername_returns_true_when_either_matches() {
    val username = FakeGenerator.username();
    val email = FakeGenerator.email();
    createUser(username, email);

    assertThat(
            userRepository.existsByEmailOrUsername(
                Email.fromString(email), UserName.fromString(FakeGenerator.username())))
        .isTrue();
    assertThat(
            userRepository.existsByEmailOrUsername(
                Email.fromString(FakeGenerator.email()), UserName.fromString(username)))
        .isTrue();
  }

  @Test
  void existsByEmailOrUsername_returns_false_when_none_match() {
    val exists =
        userRepository.existsByEmailOrUsername(
            Email.fromString(FakeGenerator.email()), UserName.fromString(FakeGenerator.username()));

    assertThat(exists).isFalse();
  }

  @Test
  void updateById_updates_fields_and_returns_true() {
    val created = createUser(FakeGenerator.username(), FakeGenerator.email());
    val updateData =
        UpdateUserDbo.builder()
            .username(UserName.fromString(FakeGenerator.username()))
            .email(Email.fromString(FakeGenerator.email()))
            .hashedPassword("updated-hash")
            .isEmailVerified(true)
            .isEnabled(false)
            .build();

    val updated = userRepository.updateById(created.id(), updateData);

    assertThat(updated).isTrue();
    val found = userRepository.findById(created.id()).orElseThrow();
    assertThat(found.username()).isEqualTo(updateData.getUsername());
    assertThat(found.email()).isEqualTo(updateData.getEmail());
    assertThat(found.hashedPassword()).isEqualTo(updateData.getHashedPassword());
    assertThat(found.isEmailVerified()).isTrue();
    assertThat(found.isEnabled()).isFalse();
  }

  @Test
  void updateById_returns_false_when_missing() {
    val updateData = UpdateUserDbo.builder().email(Email.fromString(FakeGenerator.email())).build();

    val updated =
        userRepository.updateById(UserId.fromUuid(java.util.UUID.randomUUID()), updateData);

    assertThat(updated).isFalse();
  }

  @Test
  void deleteById_deletes_user() {
    val created = createUser(FakeGenerator.username(), FakeGenerator.email());

    val deleted = userRepository.deleteById(created.id());

    assertThat(deleted).isTrue();
    assertThat(userRepository.findById(created.id()).isEmpty()).isTrue();
  }

  @Test
  void deleteById_returns_false_when_missing() {
    val deleted = userRepository.deleteById(UserId.fromUuid(java.util.UUID.randomUUID()));

    assertThat(deleted).isFalse();
  }

  @Test
  void deleteByIsUnverifiedAndOlderThan_deletes_only_old_unverified() {
    val now = OffsetDateTime.now(ZoneOffset.UTC);
    val oldUnverified =
        insertUserWithFields(
            FakeGenerator.username(), FakeGenerator.email(), now.minusDays(3), false, true);
    val recentUnverified =
        insertUserWithFields(
            FakeGenerator.username(), FakeGenerator.email(), now.minusHours(12), false, true);
    val oldVerified =
        insertUserWithFields(
            FakeGenerator.username(), FakeGenerator.email(), now.minusDays(4), true, true);

    userRepository.deleteByIsUnverifiedAndOlderThan(Duration.ofDays(2));

    assertThat(userRepository.findById(oldUnverified).isEmpty()).isTrue();
    assertThat(userRepository.findById(recentUnverified).isPresent()).isTrue();
    assertThat(userRepository.findById(oldVerified).isPresent()).isTrue();
  }

  @Test
  void findAll_returns_page_sorted_by_created_at_desc() {
    val now = OffsetDateTime.now(ZoneOffset.UTC);
    val oldest =
        insertUserWithFields(
            FakeGenerator.username(), FakeGenerator.email(), now.minusDays(3), true, true);
    val middle =
        insertUserWithFields(
            FakeGenerator.username(), FakeGenerator.email(), now.minusDays(2), true, true);
    val newest =
        insertUserWithFields(
            FakeGenerator.username(), FakeGenerator.email(), now.minusDays(1), true, true);

    val page = userRepository.findAll(PageRequest.of(0, 2));

    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
    assertThat(page.getContent().get(0).id()).isEqualTo(newest);
    assertThat(page.getContent().get(1).id()).isEqualTo(middle);
    assertThat(page.getContent()).noneMatch(user -> user.id().equals(oldest));
  }
}
