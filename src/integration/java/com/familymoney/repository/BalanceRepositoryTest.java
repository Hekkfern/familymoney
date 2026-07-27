package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.repositories.BalanceRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateBalanceDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateBalanceDto;
import com.familymoney.domains.transactions.repositories.entitites.BalanceEntity;
import com.familymoney.domains.transactions.types.BalanceId;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
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

  private GroupId insertRandomGroup() {
    val groupId = GroupId.generate();
    val now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext,
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        "desc",
        Monetary.getCurrency("USD"),
        now);
    return groupId;
  }

  private BalanceId insertRandomBalance(
      final GroupId groupId, final UserId userId1, final UserId userId2) {
    val balanceId = BalanceId.generate();
    DatabaseCrud.insertBalance(
        dslContext, balanceId, groupId, Money.of(10, "USD"), userId1, userId2);
    return balanceId;
  }

  // region IBalanceRepository.create()

  @Test
  void create_persists_balance_record() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val balanceId = BalanceId.generate();
    val money = Money.of(23, "USD");

    val balanceOpt =
        balanceRepository.create(new CreateBalanceDto(balanceId, groupId, userId1, userId2, money));

    assertThat(balanceOpt).isPresent();
    val balance = balanceOpt.get();
    assertThat(balance.id()).isEqualTo(balanceId);
    assertThat(balance.groupId()).isEqualTo(groupId);
    assertThat(balance.money()).isEqualTo(money);
    assertThat(balance.user1()).isEqualTo(userId1);
    assertThat(balance.user2()).isEqualTo(userId2);
  }

  @Test
  void create_throws_when_user_does_not_exist() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = UserId.generate();
    val money = Money.of(23, "USD");

    val dto = new CreateBalanceDto(BalanceId.generate(), groupId, userId1, userId2, money);
    assertThatThrownBy(() -> balanceRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_it_is_duplicate() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val money = Money.of(23, "USD");

    balanceRepository.create(
        new CreateBalanceDto(BalanceId.generate(), groupId, userId1, userId2, money));

    val dto = new CreateBalanceDto(BalanceId.generate(), groupId, userId2, userId1, money);
    assertThatThrownBy(() -> balanceRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_users_are_same() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val money = Money.of(23, "USD");

    val dto = new CreateBalanceDto(BalanceId.generate(), groupId, userId1, userId1, money);
    assertThatThrownBy(() -> balanceRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // endregion

  // region IBalanceRepository.findByGroup()

  @Test
  void findByGroup_returns_all_balances_in_group() {
    val groupId1 = insertRandomGroup();
    val groupId2 = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val userId3 = insertRandomUser();
    val balanceGroup1 = insertRandomBalance(groupId1, userId1, userId2);
    insertRandomBalance(groupId2, userId1, userId3);

    val balances = balanceRepository.findByGroup(groupId1);

    assertThat(balances)
        .isNotEmpty()
        .extracting(BalanceEntity::id)
        .containsExactlyInAnyOrder(balanceGroup1);
  }

  @Test
  void findByGroup_returns_empty_list_when_no_balances_found() {
    // TODO
  }

  // endregion

  // region IBalanceRepository.findByUserAndGroup()

  @Test
  void findByUserAndGroup_returns_balances_for_user() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val userId3 = insertRandomUser();
    val balance1 = insertRandomBalance(groupId, userId1, userId2);
    val balance2 = insertRandomBalance(groupId, userId2, userId3);

    val balancesForUser1 = balanceRepository.findByUserAndGroup(userId1, groupId);

    assertThat(balancesForUser1)
        .isNotEmpty()
        .extracting(BalanceEntity::id)
        .containsExactlyInAnyOrder(balance1);
  }

  @Test
  void findByUserAndGroup_returns_empty_list_when_no_balances_found() {
    // TODO
  }

  // endregion

  // region IBalanceRepository.updateById()

  @Test
  void updateById_updates_balance() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val userId3 = insertRandomUser();
    val balance1 = insertRandomBalance(groupId, userId1, userId2);
    val newMoney = Money.of(15.75, "USD");
    val dataToUpdate = UpdateBalanceDto.builder().money(newMoney).user2(userId3).build();

    val updated = balanceRepository.updateById(balance1, dataToUpdate);

    assertThat(updated).isTrue();
    val found = balanceRepository.findById(balance1).orElseThrow();
    assertThat(found.money()).isEqualTo(newMoney);
    assertThat(found.user2()).isEqualTo(userId3);
  }

  @Test
  void updateById_throws_when_user_does_not_exist() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val userId3 = UserId.generate();
    val balance1 = insertRandomBalance(groupId, userId1, userId2);
    val dataToUpdate = UpdateBalanceDto.builder().user1(userId3).build();

    assertThatThrownBy(() -> balanceRepository.updateById(balance1, dataToUpdate))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // endregion

  // region IBalanceRepository.findById()

  @Test
  void findById_returns_balance_when_it_exists() {
    val groupId = insertRandomGroup();
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    val balance1 = insertRandomBalance(groupId, userId1, userId2);

    val balanceFoundOpt = balanceRepository.findById(balance1);

    assertThat(balanceFoundOpt).isPresent();
    val balanceFound = balanceFoundOpt.get();
    assertThat(balanceFound.id()).isEqualTo(balance1);
  }

  @Test
  void findById_returns_empty_when_it_does_not_exist() {
    val found = balanceRepository.findById(BalanceId.fromUuid(UUID.randomUUID()));

    assertThat(found).isEmpty();
  }

  // endregion
}
