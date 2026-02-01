package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.familymoney.familymoney.repositories.UserRepository;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserName;
import com.familymoney.familymoney.utils.FakeGenerator;
import lombok.val;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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

  @Test
  void create_persists_user_record() {
    val username = UserName.fromString(FakeGenerator.username());
    val email = Email.fromString(FakeGenerator.email());
    val passwordHash = "hashed-password";

    val created = userRepository.create(username, email, passwordHash);

    assertTrue(created.isPresent());
    val user = created.get();
    assertNotNull(user.id());
    assertEquals(username, user.username());
    assertEquals(email, user.email());
    assertEquals(passwordHash, user.hashedPassword());
    assertNotNull(user.createdAt());
    assertNotNull(user.updatedAt());
  }
}
