package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.familymoney.generated.tables.Groups;
import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.BalanceRepository;
import com.familymoney.familymoney.repositories.dbos.BalanceDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateBalanceDbo;
import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.util.UUID;
import javax.money.Monetary;
import lombok.val;
import org.javamoney.moneta.Money;
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
public class BalanceRepositoryTests {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private BalanceRepository balanceRepository;

  @BeforeEach
  void setUp() {
    this.balanceRepository = new BalanceRepository(dslContext);
  }

  private UserId insertUser(final String username, final String email) {
    val record =
        dslContext
            .insertInto(Users.USERS)
            .columns(Users.USERS.USERNAME, Users.USERS.EMAIL, Users.USERS.HASHED_PASSWORD)
            .values(username, email, "hashed-password")
            .returning(Users.USERS.ID)
            .fetchOne();
    return UserId.fromUuid(record.getId());
  }

  private GroupId insertGroup(final String name, final String description, final String currency) {
    val record =
        dslContext
            .insertInto(Groups.GROUPS)
            .columns(Groups.GROUPS.NAME, Groups.GROUPS.DESCRIPTION, Groups.GROUPS.CURRENCY_CODE)
            .values(name, description, currency)
            .returning(Groups.GROUPS.ID)
            .fetchOne();
    return GroupId.fromUuid(record.getId());
  }

  private BalanceDbo createBalance(
      final GroupId groupId, final Money amount, final UserId user1, final UserId user2) {
    return balanceRepository.create(groupId, amount, user1, user2).orElseThrow();
  }

  @Test
  void create_persists_balance() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val amount = Money.of(12.50, Monetary.getCurrency("USD"));

    val created = balanceRepository.create(groupId, amount, user1, user2);

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.groupId()).isEqualTo(groupId);
    assertThat(dbo.amount()).isEqualTo(amount);
    assertThat(dbo.user1()).isEqualTo(user1);
    assertThat(dbo.user2()).isEqualTo(user2);
  }

  @Test
  void create_throws_when_user_missing() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val missingUser = UserId.fromUuid(UUID.randomUUID());
    val amount = Money.of(5, Monetary.getCurrency("USD"));

    assertThatThrownBy(
            () -> balanceRepository.create(groupId, amount, missingUser, missingUser))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_user_pair_is_duplicate() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val amount = Money.of(7, Monetary.getCurrency("USD"));

    balanceRepository.create(groupId, amount, user1, user2);

    assertThatThrownBy(() -> balanceRepository.create(groupId, amount, user2, user1))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_users_are_same() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val amount = Money.of(10, Monetary.getCurrency("USD"));

    assertThatThrownBy(() -> balanceRepository.create(groupId, amount, user1, user1))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findById_returns_balance_when_exists() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val amount = Money.of(9, Monetary.getCurrency("USD"));
    val created = createBalance(groupId, amount, user1, user2);

    val found = balanceRepository.findById(created.id());

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(created.id());
  }

  @Test
  void findById_returns_empty_when_missing() {
    val found = balanceRepository.findById(BalanceId.fromUuid(UUID.randomUUID()));

    assertThat(found).isEmpty();
  }

  @Test
  void findByGroup_returns_all_balances_in_group() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val otherGroupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user3 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val amount = Money.of(11, Monetary.getCurrency("USD"));
    val inGroup = createBalance(groupId, amount, user1, user2);
    createBalance(otherGroupId, amount, user1, user3);

    val balances = balanceRepository.findByGroup(groupId);

    assertThat(balances).extracting(BalanceDbo::id).contains(inGroup.id());
  }

  @Test
  void findByUserAndGroup_returns_balances_for_user() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user3 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val amount = Money.of(8, Monetary.getCurrency("USD"));
    val balance = createBalance(groupId, amount, user1, user2);
    createBalance(groupId, amount, user2, user3);

    val balancesForUser1 = balanceRepository.findByUserAndGroup(user1, groupId);

    assertThat(balancesForUser1).extracting(BalanceDbo::id).contains(balance.id());
  }

  @Test
  void updateById_updates_balance() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user3 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created =
        createBalance(groupId, Money.of(4, Monetary.getCurrency("USD")), user1, user2);
    val update =
        UpdateBalanceDbo.builder()
            .amount(Money.of(15.75, Monetary.getCurrency("USD")))
            .user2(user3)
            .build();

    val updated = balanceRepository.updateById(created.id(), update);

    assertThat(updated).isTrue();
    val found = balanceRepository.findById(created.id()).orElseThrow();
    assertThat(found.amount()).isEqualTo(update.getAmount());
    assertThat(found.user2()).isEqualTo(user3);
  }

  @Test
  void updateById_returns_false_when_missing() {
    val update = UpdateBalanceDbo.builder().user1(UserId.fromUuid(UUID.randomUUID())).build();

    val updated = balanceRepository.updateById(BalanceId.fromUuid(UUID.randomUUID()), update);

    assertThat(updated).isFalse();
  }
}
