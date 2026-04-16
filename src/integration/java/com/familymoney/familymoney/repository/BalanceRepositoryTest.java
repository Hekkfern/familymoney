package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.familymoney.generated.tables.Groups;
import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.dtos.CreateBalanceDto;
import com.familymoney.familymoney.repositories.dtos.UpdateBalanceDto;
import com.familymoney.familymoney.repositories.entities.BalanceEntity;
import com.familymoney.familymoney.repositories.impl.BalanceRepository;
import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.util.UUID;
import javax.money.CurrencyUnit;
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
class BalanceRepositoryTest {

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
    val r =
        dslContext
            .insertInto(Groups.GROUPS)
            .columns(Groups.GROUPS.NAME, Groups.GROUPS.DESCRIPTION, Groups.GROUPS.CURRENCY_CODE)
            .values(name, description, currency)
            .returning(Groups.GROUPS.ID)
            .fetchOne();
    return GroupId.fromUuid(r.getId());
  }

  private BalanceEntity createBalance(
      final GroupId groupId, final UserId user1, final UserId user2, final CurrencyUnit currency) {
    return balanceRepository
        .create(new CreateBalanceDto(any(), groupId, user1, user2, currency))
        .orElseThrow();
  }

  @Test
  void create_persists_balance() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());

    val created =
        balanceRepository.create(new CreateBalanceDto(any(), groupId, user1, user2, currency));

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.groupId()).isEqualTo(groupId);
    assertThat(dbo.amount()).isEqualTo(Money.zero(currency));
    assertThat(dbo.user1()).isEqualTo(user1);
    assertThat(dbo.user2()).isEqualTo(user2);
  }

  @Test
  void create_throws_when_user_missing() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val missingUser1 = UserId.fromUuid(UUID.randomUUID());
    val missingUser2 = UserId.fromUuid(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                balanceRepository.create(
                    new CreateBalanceDto(any(), groupId, missingUser1, missingUser2, currency)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_user_pair_is_duplicate() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());

    balanceRepository.create(new CreateBalanceDto(any(), groupId, user1, user2, currency));

    assertThatThrownBy(
            () ->
                balanceRepository.create(
                    new CreateBalanceDto(any(), groupId, user2, user1, currency)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_users_are_same() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());

    assertThatThrownBy(
            () ->
                balanceRepository.create(
                    new CreateBalanceDto(any(), groupId, user1, user1, currency)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findById_returns_balance_when_exists() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createBalance(groupId, user1, user2, currency);

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
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val otherGroupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user3 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val inGroup = createBalance(groupId, user1, user2, currency);
    createBalance(otherGroupId, user1, user3, currency);

    val balances = balanceRepository.findByGroup(groupId);

    assertThat(balances).extracting(BalanceEntity::id).contains(inGroup.id());
  }

  @Test
  void findByUserAndGroup_returns_balances_for_user() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user3 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val balance = createBalance(groupId, user1, user2, currency);
    createBalance(groupId, user2, user3, currency);

    val balancesForUser1 = balanceRepository.findByUserAndGroup(user1, groupId);

    assertThat(balancesForUser1).extracting(BalanceEntity::id).contains(balance.id());
  }

  @Test
  void updateById_updates_balance() {
    val currency = Monetary.getCurrency("USD");
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user3 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createBalance(groupId, user1, user2, currency);
    val update = UpdateBalanceDto.builder().amount(Money.of(15.75, currency)).user2(user3).build();

    val updated = balanceRepository.updateById(created.id(), update);

    assertThat(updated).isTrue();
    val found = balanceRepository.findById(created.id()).orElseThrow();
    assertThat(found.amount()).isEqualTo(update.getAmount());
    assertThat(found.user2()).isEqualTo(user3);
  }

  @Test
  void updateById_returns_false_when_missing() {
    val update = UpdateBalanceDto.builder().user1(UserId.fromUuid(UUID.randomUUID())).build();

    val updated = balanceRepository.updateById(BalanceId.fromUuid(UUID.randomUUID()), update);

    assertThat(updated).isFalse();
  }
}
