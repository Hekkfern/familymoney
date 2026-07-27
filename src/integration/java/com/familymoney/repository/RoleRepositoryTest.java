package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.users.repositories.RoleRepository;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.Role;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
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

  @BeforeEach
  void setUp() {
    this.roleRepository = new RoleRepository(dslContext);
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

  // region IRoleRepository.getRoleByUserId()

  @Test
  void getRoleByUserId_returns_empty_when_not_role_assigned() {
    val userId = insertRandomUser();

    val role = roleRepository.getRoleByUserId(userId);

    assertThat(role).isEmpty();
  }

  @Test
  void getRoleByUserId_returns_empty_when_user_missing() {
    val missingUserId = UserId.generate();

    val role = roleRepository.getRoleByUserId(missingUserId);

    assertThat(role).isEmpty();
  }

  @Test
  void getRoleForUserId_returns_role_when_role_assigned() {
    val userId = insertRandomUser();
    val updated = roleRepository.setRoleForUserId(userId, Role.USER);
    assertThat(updated).isTrue();

    val role = roleRepository.getRoleByUserId(userId);

    assertThat(role).contains(Role.USER);
  }

  // endregion

  // region IRoleRepository.getRoleByUserId()

  @Test
  void setRoleForUserId_updates_existing_role() {
    val userId = insertRandomUser();
    roleRepository.setRoleForUserId(userId, Role.USER);

    val updated = roleRepository.setRoleForUserId(userId, Role.ADMIN);

    assertThat(updated).isTrue();
    assertThat(roleRepository.getRoleByUserId(userId)).contains(Role.ADMIN);
  }

  @Test
  void setRoleForUserId_throws_when_user_missing() {
    val missingUserId = UserId.generate();

    assertThatThrownBy(() -> roleRepository.setRoleForUserId(missingUserId, Role.USER))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // endregion

}
