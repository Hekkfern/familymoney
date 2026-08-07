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
import com.familymoney.test_utils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
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
class BalanceRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private BalanceRepository balanceRepository;
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    this.balanceRepository = new BalanceRepository(dslContext);
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

  private GroupId insertRandomGroup() {
    final GroupId groupId = GroupId.generate();
    final Instant now = Instant.now();
    databaseCrud.insertGroup(
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        "desc",
        Monetary.getCurrency("USD"),
        now);
    return groupId;
  }

  private BalanceId insertRandomBalance(
      final GroupId groupId, final UserId userId1, final UserId userId2) {
    final BalanceId balanceId = BalanceId.generate();
    databaseCrud.insertBalance(balanceId, groupId, Money.of(10, "USD"), userId1, userId2);
    return balanceId;
  }

  @Nested
  class Create {

    @Test
    void persists_balance_record() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final BalanceId balanceId = BalanceId.generate();
      final Money money = Money.of(23, "USD");

      final Optional<BalanceEntity> balanceOpt =
          balanceRepository.create(
              new CreateBalanceDto(balanceId, groupId, userId1, userId2, money));

      assertThat(balanceOpt).isPresent();
      final BalanceEntity balance = balanceOpt.get();
      assertThat(balance.id()).isEqualTo(balanceId);
      assertThat(balance.groupId()).isEqualTo(groupId);
      assertThat(balance.money()).isEqualTo(money);
      assertThat(balance.user1()).isEqualTo(userId1);
      assertThat(balance.user2()).isEqualTo(userId2);
    }

    @Test
    void throws_when_user_does_not_exist() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = UserId.generate();
      final Money money = Money.of(23, "USD");

      final CreateBalanceDto dto =
          new CreateBalanceDto(BalanceId.generate(), groupId, userId1, userId2, money);
      assertThatThrownBy(() -> balanceRepository.create(dto))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throws_when_it_is_duplicate() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final Money money = Money.of(23, "USD");

      balanceRepository.create(
          new CreateBalanceDto(BalanceId.generate(), groupId, userId1, userId2, money));

      final CreateBalanceDto dto =
          new CreateBalanceDto(BalanceId.generate(), groupId, userId2, userId1, money);
      assertThatThrownBy(() -> balanceRepository.create(dto))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throws_when_users_are_same() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final Money money = Money.of(23, "USD");

      final CreateBalanceDto dto =
          new CreateBalanceDto(BalanceId.generate(), groupId, userId1, userId1, money);
      assertThatThrownBy(() -> balanceRepository.create(dto))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  class FindByGroup {

    @Test
    void returns_all_balances_in_group() {
      final GroupId groupId1 = insertRandomGroup();
      final GroupId groupId2 = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final UserId userId3 = insertRandomUser();
      final BalanceId balanceGroup1 = insertRandomBalance(groupId1, userId1, userId2);
      insertRandomBalance(groupId2, userId1, userId3);

      final List<BalanceEntity> balances = balanceRepository.findByGroup(groupId1);

      assertThat(balances)
          .isNotEmpty()
          .extracting(BalanceEntity::id)
          .containsExactlyInAnyOrder(balanceGroup1);
    }

    @Test
    void returns_empty_list_when_no_balances_found() {
      // TODO
    }
  }

  @Nested
  class FindByUserAndGroup {

    @Test
    void returns_balances_for_user() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final UserId userId3 = insertRandomUser();
      final BalanceId balance1 = insertRandomBalance(groupId, userId1, userId2);
      final BalanceId balance2 = insertRandomBalance(groupId, userId2, userId3);

      final List<BalanceEntity> balancesForUser1 =
          balanceRepository.findByUserAndGroup(userId1, groupId);

      assertThat(balancesForUser1)
          .isNotEmpty()
          .extracting(BalanceEntity::id)
          .containsExactlyInAnyOrder(balance1);
    }

    @Test
    void returns_empty_list_when_no_balances_found() {
      // TODO
    }
  }

  @Nested
  class UpdateById {

    @Test
    void updates_balance() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final UserId userId3 = insertRandomUser();
      final BalanceId balance1 = insertRandomBalance(groupId, userId1, userId2);
      final Money newMoney = Money.of(15.75, "USD");
      final UpdateBalanceDto dataToUpdate =
          UpdateBalanceDto.builder().money(newMoney).user2(userId3).build();

      final boolean updated = balanceRepository.updateById(balance1, dataToUpdate);

      assertThat(updated).isTrue();
      final BalanceEntity found = balanceRepository.findById(balance1).orElseThrow();
      assertThat(found.money()).isEqualTo(newMoney);
      assertThat(found.user2()).isEqualTo(userId3);
    }

    @Test
    void throws_when_user_does_not_exist() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final UserId userId3 = UserId.generate();
      final BalanceId balance1 = insertRandomBalance(groupId, userId1, userId2);
      final UpdateBalanceDto dataToUpdate = UpdateBalanceDto.builder().user1(userId3).build();

      assertThatThrownBy(() -> balanceRepository.updateById(balance1, dataToUpdate))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  class FindById {

    @Test
    void returns_balance_when_it_exists() {
      final GroupId groupId = insertRandomGroup();
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      final BalanceId balance1 = insertRandomBalance(groupId, userId1, userId2);

      final Optional<BalanceEntity> balanceFoundOpt = balanceRepository.findById(balance1);

      assertThat(balanceFoundOpt).isPresent();
      final BalanceEntity balanceFound = balanceFoundOpt.get();
      assertThat(balanceFound.id()).isEqualTo(balance1);
    }

    @Test
    void returns_empty_when_it_does_not_exist() {
      final Optional<BalanceEntity> found =
          balanceRepository.findById(BalanceId.fromUuid(UUID.randomUUID()));

      assertThat(found).isEmpty();
    }
  }
}
