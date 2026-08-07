package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.repositories.dtos.CreateUserDto;
import com.familymoney.domains.users.repositories.dtos.UpdateUserDto;
import com.familymoney.domains.users.repositories.entitites.UserEntity;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.test_utils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
@ActiveProfiles("test")
class UserRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private UserRepository userRepository;
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    this.userRepository = new UserRepository(dslContext);
    this.databaseCrud = new DatabaseCrud(dslContext);
  }

  private List<UserId> insertThreeUsersForTesting() {
    final UserId userId1 = UserId.generate();
    final UserId userId2 = UserId.generate();
    final UserId userId3 = UserId.generate();
    final UserName username1 = UserName.fromString("username1");
    final UserName username2 = UserName.fromString("username2");
    final UserName username3 = UserName.fromString("username3");
    final Email email1 = Email.fromString(FakeGenerator.email());
    final Email email2 = Email.fromString(FakeGenerator.email());
    final Email email3 = Email.fromString(FakeGenerator.email());
    final Instant now = Instant.ofEpochSecond(1778755330);
    databaseCrud.insertUser(userId1, username1, email1, "pass1", now, true, true);
    databaseCrud.insertUser(userId2, username2, email2, "pass2", now.plusSeconds(1000), true, true);
    databaseCrud.insertUser(userId3, username3, email3, "pass3", now.plusSeconds(2000), true, true);
    return List.of(userId1, userId2, userId3);
  }

  @Nested
  class Create {

    @Test
    void persists_user_record() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final String passwordHash = "hashed-password";

      final Instant now = Instant.now();

      final Optional<UserEntity> userCreated =
          userRepository.create(
              new CreateUserDto(userId, username, email, passwordHash, true, false));

      assertThat(userCreated).isPresent();
      final UserEntity user = userCreated.get();
      assertThat(user.id()).isNotNull().isEqualTo(userId);
      assertThat(user.username()).isNotNull().isEqualTo(username);
      assertThat(user.email()).isNotNull().isEqualTo(email);
      assertThat(user.hashedPassword()).isNotNull().isEqualTo(passwordHash);
      assertThat(user.createdAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(user.updatedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(user.isEnabled()).isTrue();
      assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void throws_when_email_is_duplicate() {
      final UserId userId1 = UserId.generate();
      final UserId userId2 = UserId.generate();
      final UserName username1 = UserName.fromString(FakeGenerator.username());
      final UserName username2 = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final String passwordHash = "hashed-password";

      final CreateUserDto dto1 =
          new CreateUserDto(userId1, username1, email, passwordHash, true, false);
      userRepository.create(dto1);

      final CreateUserDto dto2 =
          new CreateUserDto(userId2, username2, email, passwordHash, true, false);
      assertThatThrownBy(() -> userRepository.create(dto2))
          .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void throws_when_username_is_duplicate() {
      final UserId userId1 = UserId.generate();
      final UserId userId2 = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email1 = Email.fromString(FakeGenerator.email());
      final Email email2 = Email.fromString(FakeGenerator.email());
      final String passwordHash = "hashed-password";

      final CreateUserDto dto1 =
          new CreateUserDto(userId1, username, email1, passwordHash, true, false);
      userRepository.create(dto1);

      final CreateUserDto dto2 =
          new CreateUserDto(userId2, username, email2, passwordHash, true, false);
      assertThatThrownBy(() -> userRepository.create(dto2))
          .isInstanceOf(DuplicateKeyException.class);
    }
  }

  @Nested
  class FindById {

    @Test
    void returns_user_when_exists() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);

      final Optional<UserEntity> found = userRepository.findById(userId);

      assertThat(found).isPresent();
      final UserEntity userFound = found.get();
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
    void returns_empty_when_missing() {
      final UserId userId = UserId.generate();

      final Optional<UserEntity> found = userRepository.findById(userId);

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class FindByEmail {

    @Test
    void returns_user_when_exists() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);

      final Optional<UserEntity> found = userRepository.findByEmail(email);

      assertThat(found).isPresent();
      final UserEntity userFound = found.get();
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
    void returns_empty_when_missing() {
      final Email email = Email.fromString(FakeGenerator.email());

      final Optional<UserEntity> found = userRepository.findByEmail(email);

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class FindByUsername {

    @Test
    void returns_user_when_exists() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);

      final Optional<UserEntity> found = userRepository.findByUsername(username);

      assertThat(found).isPresent();
      final UserEntity userFound = found.get();
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
    void returns_empty_when_missing() {
      final UserName username = UserName.fromString(FakeGenerator.username());

      final Optional<UserEntity> found = userRepository.findByUsername(username);

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class ExistsByEmailOrUsername {

    @Test
    void returns_true_when_either_matches() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);

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
    void returns_true_when_both_match() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);

      assertThat(userRepository.existsByEmailOrUsername(email, username)).isTrue();
    }

    @Test
    void returns_false_when_none_match() {
      assertThat(
              userRepository.existsByEmailOrUsername(
                  Email.fromString(FakeGenerator.email()),
                  UserName.fromString(FakeGenerator.username())))
          .isFalse();
    }
  }

  @Nested
  class ExistsById {

    @Test
    void returns_true_when_it_exists() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);

      assertThat(userRepository.existsById(userId)).isTrue();
    }

    @Test
    void returns_false_when_it_doesnt_exist() {
      final UserId otherUserId = UserId.generate();
      assertThat(userRepository.existsById(otherUserId)).isFalse();
    }
  }

  @Nested
  class UpdateById {

    @Test
    void updates_all_fields_and_returns_true_when_updates_all_fields() {
      final UserId userId = UserId.generate();
      final UserName username1 = UserName.fromString(FakeGenerator.username());
      final UserName username2 = UserName.fromString(FakeGenerator.username());
      final Email email1 = Email.fromString(FakeGenerator.email());
      final Email email2 = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username1, email1, "hashed_password", now, false, true);

      final UpdateUserDto dataToUpdate =
          UpdateUserDto.builder()
              .username(username2)
              .email(email2)
              .hashedPassword("updated-hash")
              .isEmailVerified(true)
              .isEnabled(false)
              .build();

      final boolean updated = userRepository.updateById(userId, dataToUpdate);

      assertThat(updated).isTrue();
      final Optional<UserEntity> found = userRepository.findById(userId);
      assertThat(found).isPresent();
      final UserEntity userFound = found.get();
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
    void updates_some_fields_and_returns_true_when_updates_some_fields() {
      final UserId userId = UserId.generate();
      final UserName username1 = UserName.fromString(FakeGenerator.username());
      final UserName username2 = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username1, email, "hashed_password", now, false, true);

      final UpdateUserDto dataToUpdate = UpdateUserDto.builder().username(username2).build();

      final boolean updated = userRepository.updateById(userId, dataToUpdate);

      assertThat(updated).isTrue();
      final Optional<UserEntity> found = userRepository.findById(userId);
      assertThat(found).isPresent();
      final UserEntity userFound = found.get();
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
    void returns_false_when_userid_not_found() {
      final UserId userId = UserId.generate();

      final UpdateUserDto updateData =
          UpdateUserDto.builder().email(Email.fromString(FakeGenerator.email())).build();

      final boolean updated = userRepository.updateById(userId, updateData);

      assertThat(updated).isFalse();
    }
  }

  @Nested
  class DeleteById {

    @Test
    void returns_true_when_user_exists() {
      final UserId userId = UserId.generate();
      final UserName username = UserName.fromString(FakeGenerator.username());
      final Email email = Email.fromString(FakeGenerator.email());
      final Instant now = Instant.now();
      databaseCrud.insertUser(userId, username, email, "hashed_password", now, false, true);
      assertThat(userRepository.findById(userId)).isPresent();

      final boolean deleted = userRepository.deleteById(userId);

      assertThat(deleted).isTrue();
      assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    void returns_false_when_user_doesnt_exist() {
      final UserId userId = UserId.generate();

      final boolean deleted = userRepository.deleteById(userId);

      assertThat(deleted).isFalse();
    }
  }

  @Nested
  class GetAll {

    @Test
    void returns_page_with_2_items_when_requests_page_0_with_size_2_and_default_sort() {
      final List<UserId> userIdsForTesting = insertThreeUsersForTesting();

      final Page<UserEntity> page = userRepository.getAll(PageRequest.of(0, 2));

      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getNumberOfElements()).isEqualTo(2);
      final List<UserId> ids = page.getContent().stream().map(UserEntity::id).toList();
      assertThat(ids.get(0)).isEqualTo(userIdsForTesting.get(2));
      assertThat(ids.get(1)).isEqualTo(userIdsForTesting.get(1));
    }

    @Test
    void returns_page_with_3_items_when_requests_page_0_with_size_4_and_default_sort() {
      final List<UserId> userIdsForTesting = insertThreeUsersForTesting();

      final Page<UserEntity> page = userRepository.getAll(PageRequest.of(0, 4));

      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getNumberOfElements()).isEqualTo(3);
      final List<UserId> ids = page.getContent().stream().map(UserEntity::id).toList();
      assertThat(ids.get(0)).isEqualTo(userIdsForTesting.get(2));
      assertThat(ids.get(1)).isEqualTo(userIdsForTesting.get(1));
      assertThat(ids.get(2)).isEqualTo(userIdsForTesting.get(0));
    }

    @Test
    void returns_page_with_1_item_when_requests_page_1_with_size_2_and_default_sort() {
      final List<UserId> userIdsForTesting = insertThreeUsersForTesting();

      final Page<UserEntity> page = userRepository.getAll(PageRequest.of(1, 2));

      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getNumberOfElements()).isEqualTo(1);
      final List<UserId> ids = page.getContent().stream().map(UserEntity::id).toList();
      assertThat(ids.getFirst()).isEqualTo(userIdsForTesting.get(0));
    }

    @Test
    void returns_empty_page_when_offset_exceeds_total() {
      insertThreeUsersForTesting();

      final Page<UserEntity> page = userRepository.getAll(PageRequest.of(100, 2));

      assertThat(page.getNumberOfElements()).isZero();
      assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void returns_page_with_2_items_when_requests_page_0_with_size_2_and_order_by_username_asc() {
      final List<UserId> userIdsForTesting = insertThreeUsersForTesting();

      final Page<UserEntity> page =
          userRepository.getAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "username")));

      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getNumberOfElements()).isEqualTo(2);
      final List<UserId> ids = page.getContent().stream().map(UserEntity::id).toList();
      assertThat(ids.get(0)).isEqualTo(userIdsForTesting.get(0));
      assertThat(ids.get(1)).isEqualTo(userIdsForTesting.get(1));
    }

    @Test
    void returns_page_with_2_items_when_requests_page_0_with_size_2_and_order_by_username_desc() {
      final List<UserId> userIdsForTesting = insertThreeUsersForTesting();

      final Page<UserEntity> page =
          userRepository.getAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "username")));

      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getNumberOfElements()).isEqualTo(2);
      final List<UserId> ids = page.getContent().stream().map(UserEntity::id).toList();
      assertThat(ids.get(0)).isEqualTo(userIdsForTesting.get(2));
      assertThat(ids.get(1)).isEqualTo(userIdsForTesting.get(1));
    }

    @Test
    void returns_page_with_2_items_when_requests_page_0_with_size_2_and_order_by_createdAt_asc() {
      final List<UserId> userIdsForTesting = insertThreeUsersForTesting();

      final Page<UserEntity> page =
          userRepository.getAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "created_at")));

      assertThat(page.getTotalElements()).isEqualTo(3);
      assertThat(page.getNumberOfElements()).isEqualTo(2);
      final List<UserId> ids = page.getContent().stream().map(UserEntity::id).toList();
      assertThat(ids.get(0)).isEqualTo(userIdsForTesting.get(0));
      assertThat(ids.get(1)).isEqualTo(userIdsForTesting.get(1));
    }
  }
}
