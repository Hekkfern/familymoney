package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;

import com.familymoney.domains.transactions.repositories.TransactionRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
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
    this.transactionRepository = new TransactionRepository(dslContext);
  }

  // region ITransactionRepository.create()

  // TODO

  // endregion

  // region ITransactionRepository.updateById()

  // TODO

  // endregion

  // region ITransactionRepository.deleteById()

  // TODO

  // endregion

  // region ITransactionRepository.findById()

  // TODO

  // endregion

  // region ITransactionRepository.findAllByGroupId()

  // TODO

  // endregion
}
