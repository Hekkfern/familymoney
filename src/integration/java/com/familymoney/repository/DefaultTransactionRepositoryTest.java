package com.familymoney.repository;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;
import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.familymoney.domains.transactions.controllers.DefaultGroupController;
import com.familymoney.domains.transactions.controllers.dtos.ExpenseTransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.PaymentTransactionDto;
import com.familymoney.domains.transactions.controllers.dtos.TransactionDto;
import com.familymoney.domains.transactions.controllers.mappers.TransactionDtoMapper;
import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.DefaultExpenseRepository;
import com.familymoney.domains.transactions.repositories.DefaultGroupRepository;
import com.familymoney.domains.transactions.repositories.DefaultPaymentRepository;
import com.familymoney.domains.transactions.repositories.DefaultTransactionRepository;
import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.TransactionRepository;
import com.familymoney.domains.transactions.repositories.entitites.FullExpenseEntity;
import com.familymoney.domains.transactions.repositories.entitites.PaymentEntity;
import com.familymoney.domains.transactions.repositories.entitites.TransactionReference;
import com.familymoney.domains.transactions.services.DefaultGroupChecker;
import com.familymoney.domains.transactions.services.DefaultGroupService;
import com.familymoney.domains.transactions.services.data.TransactionType;
import com.familymoney.domains.transactions.services.mappers.TransactionDataMapper;
import com.familymoney.domains.transactions.types.ExpenseId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.PaymentId;
import com.familymoney.domains.users.repositories.UserRepository;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.ExpensePayments;
import com.familymoney.generated.tables.ExpenseShares;
import com.familymoney.generated.tables.Expenses;
import com.familymoney.generated.tables.Groups;
import com.familymoney.generated.tables.Payments;
import com.familymoney.generated.tables.Users;
import com.familymoney.properties.GroupProperties;
import com.familymoney.utils.PageResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jooq.DSLContext;
import org.jooq.ExecuteListener;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class DefaultTransactionRepositoryTest {

  private static final Instant DONE_AT = Instant.parse("2026-01-01T12:00:00Z");
  private static final UUID LOW_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID HIGH_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext db;

  private TransactionRepository repository;
  private GroupId groupId;
  private UserId fromUser;
  private UserId toUser;

  @BeforeEach
  void setUp() {
    repository = new DefaultTransactionRepository(db);
    fromUser = insertUser("sender", "sender@example.com");
    toUser = insertUser("recipient", "recipient@example.com");
    groupId = insertGroup();
  }

  @Nested
  class FindAllByGroupId {

    @Test
    void combines_transactions_and_orders_by_done_at_type_and_id() {
      final TransactionReference olderPayment =
          insertPayment(groupId, UUID.randomUUID(), DONE_AT.minusSeconds(1));
      final TransactionReference lowPayment = insertPayment(groupId, LOW_ID, DONE_AT);
      final TransactionReference highExpense = insertExpense(groupId, HIGH_ID, DONE_AT);
      final TransactionReference highPayment = insertPayment(groupId, HIGH_ID, DONE_AT);
      final TransactionReference lowExpense = insertExpense(groupId, LOW_ID, DONE_AT);
      final TransactionReference newerExpense =
          insertExpense(groupId, UUID.randomUUID(), DONE_AT.plusSeconds(1));
      final GroupId otherGroup = insertGroup();
      insertExpense(otherGroup, UUID.randomUUID(), DONE_AT.plusSeconds(2));
      insertPayment(otherGroup, UUID.randomUUID(), DONE_AT.plusSeconds(2));

      final Page<TransactionReference> page =
          repository.findAllByGroupId(
              groupId, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "done_at")));

      assertThat(page.getContent())
          .containsExactly(
              newerExpense, highExpense, lowExpense, highPayment, lowPayment, olderPayment);
      assertThat(page.getTotalElements()).isEqualTo(6);
    }

    @Test
    void paginates_the_combined_result_and_preserves_the_total() {
      insertExpense(groupId, LOW_ID, DONE_AT);
      final TransactionReference payment = insertPayment(groupId, LOW_ID, DONE_AT);
      final TransactionReference olderExpense =
          insertExpense(groupId, HIGH_ID, DONE_AT.minusSeconds(1));
      insertPayment(groupId, HIGH_ID, DONE_AT.minusSeconds(2));
      insertExpense(groupId, UUID.randomUUID(), DONE_AT.minusSeconds(3));

      final Page<TransactionReference> page =
          repository.findAllByGroupId(groupId, PageRequest.of(1, 1));

      assertThat(page.getContent()).containsExactly(payment);
      assertThat(page.getTotalElements()).isEqualTo(5);
      assertThat(page.getTotalPages()).isEqualTo(5);
      assertThat(repository.findAllByGroupId(groupId, PageRequest.of(2, 1)).getContent())
          .containsExactly(olderExpense);
    }

    @Test
    void returns_an_empty_page_with_the_total_when_the_offset_exceeds_the_results() {
      insertExpense(groupId, LOW_ID, DONE_AT);
      insertPayment(groupId, LOW_ID, DONE_AT);

      final Page<TransactionReference> page =
          repository.findAllByGroupId(groupId, PageRequest.of(2, 2));

      assertThat(page.getContent()).isEmpty();
      assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void returns_an_empty_page_when_the_group_has_no_transactions() {
      insertExpense(groupId, LOW_ID, DONE_AT);
      insertPayment(groupId, LOW_ID, DONE_AT);

      final Page<TransactionReference> page =
          repository.findAllByGroupId(GroupId.generate(), PageRequest.of(0, 10));

      assertThat(page.getContent()).isEmpty();
      assertThat(page.getTotalElements()).isZero();
    }
  }

  @Nested
  class FindAllByGroupIdAndIds {

    @Test
    void loads_only_requested_expenses_and_preserves_participants_and_done_at() {
      insertExpense(groupId, LOW_ID, DONE_AT);
      insertExpense(groupId, HIGH_ID, DONE_AT.minusSeconds(1));
      final UUID unrequestedId = UUID.randomUUID();
      insertExpense(groupId, unrequestedId, DONE_AT);
      final UUID foreignId = UUID.randomUUID();
      insertExpense(insertGroup(), foreignId, DONE_AT);
      db.insertInto(ExpenseShares.EXPENSE_SHARES)
          .columns(
              ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
              ExpenseShares.EXPENSE_SHARES.USER_ID,
              ExpenseShares.EXPENSE_SHARES.AMOUNT)
          .values(LOW_ID, fromUser.value(), new BigDecimal("12.345"))
          .values(LOW_ID, toUser.value(), new BigDecimal("7.655"))
          .execute();
      db.insertInto(ExpensePayments.EXPENSE_PAYMENTS)
          .columns(
              ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
              ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
              ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
          .values(LOW_ID, fromUser.value(), new BigDecimal("20.000"))
          .execute();
      final DefaultExpenseRepository expenses = new DefaultExpenseRepository(db);

      final List<FullExpenseEntity> result =
          expenses.findAllByGroupIdAndIds(
              groupId,
              Set.of(
                  ExpenseId.fromUuid(LOW_ID),
                  ExpenseId.fromUuid(HIGH_ID),
                  ExpenseId.fromUuid(foreignId)));

      assertThat(result)
          .extracting(entity -> entity.expense().id().value())
          .containsExactlyInAnyOrder(LOW_ID, HIGH_ID);
      final FullExpenseEntity expense =
          result.stream()
              .filter(entity -> entity.expense().id().value().equals(LOW_ID))
              .findFirst()
              .orElseThrow();
      assertThat(expenses.findById(ExpenseId.fromUuid(LOW_ID))).contains(expense);
      assertThat(expenses.findById(ExpenseId.generate())).isEmpty();
      final ExpenseTransactionDto dto =
          (ExpenseTransactionDto)
              TransactionDtoMapper.toDto(
                  TransactionDataMapper.fromExpense(expense, Currency.getInstance("USD")));
      assertThat(dto.id()).isEqualTo(LOW_ID);
      assertThat(dto.description()).isEqualTo("Test expense");
      assertThat(dto.amount()).isEqualByComparingTo("20.000");
      assertThat(dto.doneAt()).isEqualTo(DONE_AT);
      assertThat(dto.payers()).containsExactly(fromUser.value());
      assertThat(dto.shares()).containsExactlyInAnyOrder(fromUser.value(), toUser.value());
    }

    @Test
    void loads_only_requested_payments_with_complete_fields() {
      insertPayment(groupId, LOW_ID, DONE_AT);
      insertPayment(groupId, HIGH_ID, DONE_AT.minusSeconds(1));
      insertPayment(groupId, UUID.randomUUID(), DONE_AT);
      final UUID foreignId = UUID.randomUUID();
      insertPayment(insertGroup(), foreignId, DONE_AT);

      final List<PaymentEntity> result =
          new DefaultPaymentRepository(db)
              .findAllByGroupIdAndIds(
                  groupId,
                  Set.of(
                      PaymentId.fromUuid(LOW_ID),
                      PaymentId.fromUuid(HIGH_ID),
                      PaymentId.fromUuid(foreignId)));

      assertThat(result)
          .extracting(entity -> entity.id().value())
          .containsExactlyInAnyOrder(LOW_ID, HIGH_ID);
      final PaymentEntity payment =
          result.stream()
              .filter(entity -> entity.id().value().equals(LOW_ID))
              .findFirst()
              .orElseThrow();
      final PaymentTransactionDto dto =
          (PaymentTransactionDto)
              TransactionDtoMapper.toDto(
                  TransactionDataMapper.fromPayment(payment, Currency.getInstance("USD")));
      assertThat(dto.id()).isEqualTo(LOW_ID);
      assertThat(dto.description()).isEqualTo("Test payment");
      assertThat(dto.amount()).isEqualByComparingTo(BigDecimal.ONE);
      assertThat(dto.doneAt()).isEqualTo(DONE_AT);
      assertThat(dto.fromUser()).isEqualTo(fromUser.value());
      assertThat(dto.toUser()).isEqualTo(toUser.value());
    }

    @Test
    void empty_ids_do_not_access_the_database() {
      final DSLContext unusedDb = mock(DSLContext.class);

      assertThat(new DefaultExpenseRepository(unusedDb).findAllByGroupIdAndIds(groupId, Set.of()))
          .isEmpty();
      assertThat(new DefaultPaymentRepository(unusedDb).findAllByGroupIdAndIds(groupId, Set.of()))
          .isEmpty();
      verifyNoInteractions(unusedDb);
    }
  }

  @Nested
  class GetGroupTransactions {

    @Test
    void controller_service_and_repositories_return_persisted_details_with_bounded_queries() {
      insertExpense(groupId, LOW_ID, DONE_AT.plusSeconds(1));
      insertPayment(groupId, LOW_ID, DONE_AT);
      insertExpense(groupId, HIGH_ID, DONE_AT.minusSeconds(1));
      insertPayment(groupId, HIGH_ID, DONE_AT.minusSeconds(2));
      insertExpense(groupId, UUID.randomUUID(), DONE_AT.minusSeconds(3));
      db.insertInto(ExpenseShares.EXPENSE_SHARES)
          .columns(
              ExpenseShares.EXPENSE_SHARES.EXPENSE_ID,
              ExpenseShares.EXPENSE_SHARES.USER_ID,
              ExpenseShares.EXPENSE_SHARES.AMOUNT)
          .values(LOW_ID, toUser.value(), new BigDecimal("12.345"))
          .execute();
      db.insertInto(ExpensePayments.EXPENSE_PAYMENTS)
          .columns(
              ExpensePayments.EXPENSE_PAYMENTS.EXPENSE_ID,
              ExpensePayments.EXPENSE_PAYMENTS.USER_ID,
              ExpensePayments.EXPENSE_PAYMENTS.AMOUNT)
          .values(LOW_ID, fromUser.value(), new BigDecimal("12.345"))
          .execute();
      final Clock clock = Clock.fixed(DONE_AT, DEFAULT_TIMEZONE_OFFSET);
      new DefaultGroupRepository(db, clock).addUserToGroup(fromUser, groupId);
      final List<String> statements = new ArrayList<>();
      final DSLContext measuredDb =
          DSL.using(
              db.configuration()
                  .derive(
                      ExecuteListener.onExecuteStart(context -> statements.add(context.sql()))));
      final DefaultGroupRepository groups = new DefaultGroupRepository(measuredDb, clock);
      final DefaultGroupService service =
          new DefaultGroupService(
              groups,
              mock(BalanceRepository.class),
              new DefaultTransactionRepository(measuredDb),
              new DefaultExpenseRepository(measuredDb),
              new DefaultPaymentRepository(measuredDb),
              mock(GroupInvitationRepository.class),
              new DefaultGroupChecker(groups, mock(UserRepository.class)),
              clock,
              mock(GroupProperties.class));
      final SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(
          UsernamePasswordAuthenticationToken.authenticated(
              fromUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
      SecurityContextHolder.setContext(context);

      final PageResponse<TransactionDto> result;
      try {
        result = new DefaultGroupController(service).getGroupTransactions(groupId.value(), 0, 20);
      } finally {
        SecurityContextHolder.clearContext();
      }

      assertThat(result.totalElements()).isEqualTo(5);
      assertThat(result.content())
          .extracting(TransactionDto::doneAt)
          .containsExactly(
              DONE_AT.plusSeconds(1),
              DONE_AT,
              DONE_AT.minusSeconds(1),
              DONE_AT.minusSeconds(2),
              DONE_AT.minusSeconds(3));
      final ExpenseTransactionDto expense = (ExpenseTransactionDto) result.content().get(0);
      assertThat(expense.amount()).isEqualByComparingTo("12.345");
      assertThat(expense.description()).isEqualTo("Test expense");
      assertThat(expense.payers()).containsExactly(fromUser.value());
      assertThat(expense.shares()).containsExactly(toUser.value());
      final PaymentTransactionDto payment = (PaymentTransactionDto) result.content().get(1);
      assertThat(payment.amount()).isEqualByComparingTo("1");
      assertThat(payment.description()).isEqualTo("Test payment");
      assertThat(payment.fromUser()).isEqualTo(fromUser.value());
      assertThat(payment.toUser()).isEqualTo(toUser.value());
      assertThat(statements)
          .hasSize(7)
          .allSatisfy(statement -> assertThat(statement).doesNotContainIgnoringCase("created_at"));
    }
  }

  private GroupId insertGroup() {
    final GroupId id = GroupId.generate();
    db.insertInto(Groups.GROUPS)
        .columns(
            Groups.GROUPS.ID,
            Groups.GROUPS.NAME,
            Groups.GROUPS.DESCRIPTION,
            Groups.GROUPS.CURRENCY_CODE,
            Groups.GROUPS.CREATED_BY)
        .values(id.value(), "Test group", "Test group", "USD", fromUser.value())
        .execute();
    return id;
  }

  private UserId insertUser(final String name, final String email) {
    final UserId id = UserId.generate();
    db.insertInto(Users.USERS)
        .columns(
            Users.USERS.ID,
            Users.USERS.USERNAME,
            Users.USERS.EMAIL,
            Users.USERS.HASHED_PASSWORD,
            Users.USERS.IS_EMAIL_VERIFIED,
            Users.USERS.IS_ENABLED)
        .values(id.value(), name, email, "hashed_password", true, true)
        .execute();
    return id;
  }

  private TransactionReference insertExpense(
      final GroupId id, final UUID expenseId, final Instant doneAt) {
    final OffsetDateTime timestamp = doneAt.atOffset(DEFAULT_TIMEZONE_OFFSET);
    db.insertInto(Expenses.EXPENSES)
        .columns(
            Expenses.EXPENSES.ID,
            Expenses.EXPENSES.GROUP_ID,
            Expenses.EXPENSES.DESCRIPTION,
            Expenses.EXPENSES.DONE_AT,
            Expenses.EXPENSES.CREATED_BY)
        .values(expenseId, id.value(), "Test expense", timestamp, fromUser.value())
        .execute();
    return new TransactionReference(expenseId, TransactionType.EXPENSE, doneAt);
  }

  private TransactionReference insertPayment(
      final GroupId id, final UUID paymentId, final Instant doneAt) {
    final OffsetDateTime timestamp = doneAt.atOffset(DEFAULT_TIMEZONE_OFFSET);
    db.insertInto(Payments.PAYMENTS)
        .columns(
            Payments.PAYMENTS.ID,
            Payments.PAYMENTS.GROUP_ID,
            Payments.PAYMENTS.DESCRIPTION,
            Payments.PAYMENTS.AMOUNT,
            Payments.PAYMENTS.FROM_USER,
            Payments.PAYMENTS.TO_USER,
            Payments.PAYMENTS.DONE_AT,
            Payments.PAYMENTS.CREATED_BY)
        .values(
            paymentId,
            id.value(),
            "Test payment",
            BigDecimal.ONE,
            fromUser.value(),
            toUser.value(),
            timestamp,
            fromUser.value())
        .execute();
    return new TransactionReference(paymentId, TransactionType.PAYMENT, doneAt);
  }
}
