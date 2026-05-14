package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.domains.user.repositories.UserRepository;
import com.familymoney.domains.user.repositories.dtos.CreateUserDto;
import com.familymoney.domains.user.repositories.dtos.UpdateUserDto;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class UserRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    this.userRepository = new UserRepository(dslContext);
  }

  // region IUserRepository.create()

  @Test
  void create_persists_user_record() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    val now = Instant.now();

    val userCreated =
        userRepository.create(
            new CreateUserDto(userId, username, email, passwordHash, true, false));

    assertThat(userCreated).isPresent();
    val user = userCreated.get();
    assertThat(user.id()).isEqualTo(userId);
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
    val userId1 = UserId.generate();
    val userId2 = UserId.generate();
    val username1 = UserName.fromString(FakeGenerator.username());
    val username2 = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    val dto1 = new CreateUserDto(userId1, username1, email, passwordHash, true, false);
    userRepository.create(dto1);

    val dto2 = new CreateUserDto(userId2, username2, email, passwordHash, true, false);
    assertThatThrownBy(() -> userRepository.create(dto2)).isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void create_throws_when_username_is_duplicate() {
    val userId1 = UserId.generate();
    val userId2 = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email1 = Email.fromString(FakeGenerator.email());
    val email2 = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    val dto1 = new CreateUserDto(userId1, username, email1, passwordHash, true, false);
    userRepository.create(dto1);

    val dto2 = new CreateUserDto(userId2, username, email2, passwordHash, true, false);
    assertThatThrownBy(() -> userRepository.create(dto2)).isInstanceOf(DuplicateKeyException.class);
  }

  // endregion

  // region IUserRepository.findById()

  @Test
  void findById_returns_user_when_exists() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);

    val found = userRepository.findById(userId);

    assertThat(found).isPresent();
    val userFound = found.get();
    assertThat(userFound.id()).isEqualTo(userId);
    assertThat(userFound.username()).isEqualTo(username);
    assertThat(userFound.email()).isEqualTo(email);
    assertThat(userFound.hashedPassword()).isEqualTo("hashed_password");
    assertThat(userFound.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.isEnabled()).isTrue();
    assertThat(userFound.isEmailVerified()).isFalse();
  }

  @Test
  void findById_returns_empty_when_missing() {
    val userId = UserId.generate();

    val found = userRepository.findById(userId);

    assertThat(found).isEmpty();
  }

  // endregion

  // region IUserRepository.findByEmail()

  @Test
  void findByEmail_returns_user_when_exists() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);

    val found = userRepository.findByEmail(email);

    assertThat(found).isPresent();
    val userFound = found.get();
    assertThat(userFound.id()).isEqualTo(userId);
    assertThat(userFound.username()).isEqualTo(username);
    assertThat(userFound.email()).isEqualTo(email);
    assertThat(userFound.hashedPassword()).isEqualTo("hashed_password");
    assertThat(userFound.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.isEnabled()).isTrue();
    assertThat(userFound.isEmailVerified()).isFalse();
  }

  @Test
  void findByEmail_returns_empty_when_missing() {
    val email = Email.fromString(FakeGenerator.email());

    val found = userRepository.findByEmail(email);

    assertThat(found).isEmpty();
  }

  // endregion

  // region IUserRepository.findByUsername()

  @Test
  void findByUsername_returns_user_when_exists() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);

    val found = userRepository.findByUsername(username);

    assertThat(found).isPresent();
    val userFound = found.get();
    assertThat(userFound.id()).isEqualTo(userId);
    assertThat(userFound.username()).isEqualTo(username);
    assertThat(userFound.email()).isEqualTo(email);
    assertThat(userFound.hashedPassword()).isEqualTo("hashed_password");
    assertThat(userFound.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.isEnabled()).isTrue();
    assertThat(userFound.isEmailVerified()).isFalse();
  }

  @Test
  void findByUsername_returns_empty_when_missing() {
    val username = UserName.fromString(FakeGenerator.username());

    val found = userRepository.findByUsername(username);

    assertThat(found).isEmpty();
  }

  // endregion

  // region IUserRepository.existsByEmailOrUsername()

  @Test
  void existsByEmailOrUsername_returns_true_when_either_matches() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);

    assertThat(
            userRepository.existsByEmailOrUsername(
                email, UserName.fromString(FakeGenerator.username())))
        .isTrue();
    assertThat(
            userRepository.existsByEmailOrUsername(
                Email.fromString(FakeGenerator.email()), username))
        .isTrue();
  }

  @Test
  void existsByEmailOrUsername_returns_true_when_both_match() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);

    assertThat(userRepository.existsByEmailOrUsername(email, username)).isTrue();
  }

  @Test
  void existsByEmailOrUsername_returns_false_when_none_match() {
    assertThat(
            userRepository.existsByEmailOrUsername(
                Email.fromString(FakeGenerator.email()),
                UserName.fromString(FakeGenerator.username())))
        .isFalse();
  }

  // endregion

  // region IUserRepository.existsById()

  @Test
  void existsById_returns_true_when_it_exists() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);

    assertThat(userRepository.existsById(userId)).isTrue();
  }

  @Test
  void existsById_returns_false_when_it_doesnt_exist() {
    val otherUserId = UserId.generate();
    assertThat(userRepository.existsById(otherUserId)).isFalse();
  }

  // endregion

  // region IUserRepository.updateById()

  @Test
  void updateById_updates_all_fields_and_returns_true_when_updates_all_fields() {
    val userId = UserId.generate();
    val username1 = UserName.fromString(FakeGenerator.username());
    val username2 = UserName.fromString(FakeGenerator.username());
    val email1 = Email.fromString(FakeGenerator.email());
    val email2 = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username1, email1, "hashed_password", now, false, true);

    val dataToUpdate =
        UpdateUserDto.builder()
            .username(username2)
            .email(email2)
            .hashedPassword("updated-hash")
            .isEmailVerified(true)
            .isEnabled(false)
            .build();

    val updated = userRepository.updateById(userId, dataToUpdate);

    assertThat(updated).isTrue();
    val found = userRepository.findById(userId);
    assertThat(found).isPresent();
    val userFound = found.get();
    assertThat(userFound.id()).isEqualTo(userId);
    assertThat(userFound.username()).isEqualTo(username2);
    assertThat(userFound.email()).isEqualTo(email2);
    assertThat(userFound.hashedPassword()).isEqualTo("updated-hash");
    assertThat(userFound.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.isEnabled()).isFalse();
    assertThat(userFound.isEmailVerified()).isTrue();
  }

  @Test
  void updateById_updates_some_fields_and_returns_true_when_updates_some_fields() {
    val userId = UserId.generate();
    val username1 = UserName.fromString(FakeGenerator.username());
    val username2 = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username1, email, "hashed_password", now, false, true);

    val dataToUpdate = UpdateUserDto.builder().username(username2).build();

    val updated = userRepository.updateById(userId, dataToUpdate);

    assertThat(updated).isTrue();
    val found = userRepository.findById(userId);
    assertThat(found).isPresent();
    val userFound = found.get();
    assertThat(userFound.id()).isEqualTo(userId);
    assertThat(userFound.username()).isEqualTo(username2);
    assertThat(userFound.email()).isEqualTo(email);
    assertThat(userFound.hashedPassword()).isEqualTo("hashed_password");
    assertThat(userFound.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.updatedAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(userFound.isEnabled()).isTrue();
    assertThat(userFound.isEmailVerified()).isFalse();
  }

  @Test
  void updateById_returns_false_when_userid_not_found() {
    val userId = UserId.generate();

    val updateData = UpdateUserDto.builder().email(Email.fromString(FakeGenerator.email())).build();

    val updated = userRepository.updateById(userId, updateData);

    assertThat(updated).isFalse();
  }

  // endregion

  // region IUserRepository.deleteById()

  @Test
  void deleteById_returns_true_when_user_exists() {
    val userId = UserId.generate();
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val now = Instant.now();
    DatabaseCrud.insertUser(
        dslContext, userId, username, email, "hashed_password", now, false, true);
    assertThat(userRepository.findById(userId)).isPresent();

    val deleted = userRepository.deleteById(userId);

    assertThat(deleted).isTrue();
    assertThat(userRepository.findById(userId)).isEmpty();
  }

  @Test
  void deleteById_returns_false_when_user_doesnt_exist() {
    val userId = UserId.generate();

    val deleted = userRepository.deleteById(userId);

    assertThat(deleted).isFalse();
  }

  // endregion

  // region IUserRepository.getAll()

  @Test
  void getAll_returns_page_when_requests_first_page_and_default_sort() {
    val userId1 = UserId.generate();
    val userId2 = UserId.generate();
    val userId3 = UserId.generate();
    val username1 = UserName.fromString(FakeGenerator.username());
    val username2 = UserName.fromString(FakeGenerator.username());
    val username3 = UserName.fromString(FakeGenerator.username());
    val email1 = Email.fromString(FakeGenerator.email());
    val email2 = Email.fromString(FakeGenerator.email());
    val email3 = Email.fromString(FakeGenerator.email());
    val now = Instant.ofEpochSecond(1778755330);
    DatabaseCrud.insertUser(dslContext, userId1, username1, email1, "pass1", now, true, true);
    DatabaseCrud.insertUser(
        dslContext, userId2, username2, email2, "pass2", now.plusSeconds(1000), true, true);
    DatabaseCrud.insertUser(
        dslContext, userId3, username3, email3, "pass3", now.plusSeconds(2000), true, true);

    val page = userRepository.getAll(PageRequest.of(0, 2));

    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
    assertThat(page.getContent().get(0).id()).isEqualTo(userId3);
    assertThat(page.getContent().get(1).id()).isEqualTo(userId2);
    assertThat(page.getContent()).noneMatch(user -> user.id().equals(userId1));
  }

  // endregion
}
