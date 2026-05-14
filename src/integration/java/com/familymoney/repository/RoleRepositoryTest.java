package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.generated.tables.Users;
import com.familymoney.domains.user.repositories.RoleRepository;
import com.familymoney.domains.user.types.Role;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.testutils.FakeGenerator;
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
  void getRoleByUserId_returns_empty_when_not_assigned() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());

    val role = roleRepository.getRoleByUserId(userId);

    assertThat(role).isEmpty();
  }

  @Test
  void setRoleForUserId_inserts_role_and_getRole_returns_it() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());

    val updated = roleRepository.setRoleForUserId(userId, Role.USER);

    assertThat(updated).isTrue();
    assertThat(roleRepository.getRoleByUserId(userId)).contains(Role.USER);
  }

  @Test
  void setRoleForUserId_updates_existing_role() {
    val userId = insertUser(FakeGenerator.username(), FakeGenerator.email());
    roleRepository.setRoleForUserId(userId, Role.USER);

    val updated = roleRepository.setRoleForUserId(userId, Role.ADMIN);

    assertThat(updated).isTrue();
    assertThat(roleRepository.getRoleByUserId(userId)).contains(Role.ADMIN);
  }

  @Test
  void setRoleForUserId_throws_when_user_missing() {
    val missingUserId = UserId.fromUuid(java.util.UUID.randomUUID());

    assertThatThrownBy(() -> roleRepository.setRoleForUserId(missingUserId, Role.USER))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void getRoleByUserId_returns_empty_when_user_missing() {
    val missingUserId = UserId.fromUuid(java.util.UUID.randomUUID());

    val role = roleRepository.getRoleByUserId(missingUserId);

    assertThat(role).isEmpty();
  }
}
