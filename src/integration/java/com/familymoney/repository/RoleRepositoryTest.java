package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.users.repositories.RoleRepository;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.repository.utils.DatabaseCrud;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class RoleRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private RoleRepository roleRepository;
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    this.roleRepository = new RoleRepository(dslContext);
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
  class GetRoleByUserId {

    @Test
    void returns_empty_when_not_role_assigned() {
      final UserId userId = insertRandomUser();

      final Optional<Role> role = roleRepository.getRoleByUserId(userId);

      assertThat(role).isEmpty();
    }

    @Test
    void returns_empty_when_user_missing() {
      final UserId missingUserId = UserId.generate();

      final Optional<Role> role = roleRepository.getRoleByUserId(missingUserId);

      assertThat(role).isEmpty();
    }

    @Test
    void returns_role_when_role_assigned() {
      final UserId userId = insertRandomUser();
      final boolean updated = roleRepository.setRoleForUserId(userId, Role.USER);
      assertThat(updated).isTrue();

      final Optional<Role> role = roleRepository.getRoleByUserId(userId);

      assertThat(role).contains(Role.USER);
    }
  }

  @Nested
  class SetRoleByUserId {

    @Test
    void updates_existing_role() {
      final UserId userId = insertRandomUser();
      roleRepository.setRoleForUserId(userId, Role.USER);

      final boolean updated = roleRepository.setRoleForUserId(userId, Role.ADMIN);

      assertThat(updated).isTrue();
      assertThat(roleRepository.getRoleByUserId(userId)).contains(Role.ADMIN);
    }

    @Test
    void throws_when_user_missing() {
      final UserId missingUserId = UserId.generate();

      assertThatThrownBy(() -> roleRepository.setRoleForUserId(missingUserId, Role.USER))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }
}
