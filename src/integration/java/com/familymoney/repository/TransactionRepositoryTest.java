package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import com.familymoney.domains.transactions.repositories.DefaultTransactionRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
@ActiveProfiles("test")
class TransactionRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private TransactionRepository transactionRepository;

  @BeforeEach
  void setUp() {
    this.transactionRepository = new DefaultTransactionRepository(dslContext);
  }

  @Nested
  class Create {

    // TODO

  }

  @Nested
  class UpdateById {

    // TODO

  }

  @Nested
  class DeleteById {

    // TODO

  }

  @Nested
  class FindById {

    // TODO

  }

  @Nested
  class FindAllByGroupId {

    // TODO

  }
}
