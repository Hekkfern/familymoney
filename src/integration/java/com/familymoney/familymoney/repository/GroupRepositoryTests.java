package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.familymoney.generated.tables.Users;
import com.familymoney.familymoney.repositories.dtos.CreateGroupDto;
import com.familymoney.familymoney.repositories.dtos.UpdateGroupDto;
import com.familymoney.familymoney.repositories.entities.GroupEntity;
import com.familymoney.familymoney.repositories.impl.GroupRepository;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.util.UUID;
import javax.money.Monetary;
import lombok.val;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class GroupRepositoryTests {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private GroupRepository groupRepository;

  @BeforeEach
  void setUp() {
    this.groupRepository = new GroupRepository(dslContext);
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

  private GroupEntity createGroup(final String name, final String description, final UserId owner) {
    return groupRepository
        .create(
            new CreateGroupDto(
                any(), GroupName.fromString(name), description, Monetary.getCurrency("USD")))
        .orElseThrow();
  }

  @Test
  void create_persists_group() {
    val currency = Monetary.getCurrency("USD");
    val name = "group-" + FakeGenerator.username();

    val created =
        groupRepository.create(
            new CreateGroupDto(any(), GroupName.fromString(name), "desc", currency));

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.name()).isEqualTo(GroupName.fromString(name));
    assertThat(dbo.description()).isEqualTo("desc");
    assertThat(dbo.currency()).isEqualTo(currency);
  }

  @Test
  void findById_returns_group_when_exists() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);

    val found = groupRepository.findById(created.id());

    assertThat(found).isPresent();
    assertThat(found.get().id()).isEqualTo(created.id());
  }

  @Test
  void findById_returns_empty_when_missing() {
    val found = groupRepository.findById(GroupId.fromUuid(UUID.randomUUID()));

    assertThat(found).isEmpty();
  }

  @Test
  void updateById_updates_group_fields() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);
    val update =
        UpdateGroupDto.builder()
            .name(GroupName.fromString("updated-" + FakeGenerator.username()))
            .description("new-desc")
            .build();

    val updated = groupRepository.updateById(created.id(), update);

    assertThat(updated).isTrue();
    val found = groupRepository.findById(created.id()).orElseThrow();
    assertThat(found.name()).isEqualTo(update.getName());
    assertThat(found.description()).isEqualTo(update.getDescription());
  }

  @Test
  void updateById_returns_false_when_missing() {
    val update = UpdateGroupDto.builder().description("new-desc").build();

    val updated = groupRepository.updateById(GroupId.fromUuid(UUID.randomUUID()), update);

    assertThat(updated).isFalse();
  }

  @Test
  void deleteById_removes_group() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);

    val deleted = groupRepository.deleteById(created.id());

    assertThat(deleted).isTrue();
    assertThat(groupRepository.findById(created.id())).isEmpty();
  }

  @Test
  void deleteById_returns_false_when_missing() {
    val deleted = groupRepository.deleteById(GroupId.fromUuid(UUID.randomUUID()));

    assertThat(deleted).isFalse();
  }

  @Test
  void addUser_adds_membership_and_queries_see_it() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);

    val added = groupRepository.addUser(user, created.id());

    assertThat(added).isPresent();
    assertThat(groupRepository.isUserInGroup(user, created.id())).isTrue();
    assertThat(groupRepository.findUserIdsByGroupId(created.id())).contains(user);
    val page = groupRepository.findByUserId(user, PageRequest.of(0, 10));
    assertThat(page.getContent()).extracting(GroupEntity::id).contains(created.id());
  }

  @Test
  void addUser_throws_when_duplicate() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);
    groupRepository.addUser(user, created.id());

    assertThatThrownBy(() -> groupRepository.addUser(user, created.id()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void isUserInGroup_returns_false_when_missing() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);

    assertThat(groupRepository.isUserInGroup(user, created.id())).isFalse();
  }

  @Test
  void deleteUser_removes_membership() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);
    groupRepository.addUser(user, created.id());

    val deleted = groupRepository.deleteUser(user, created.id());

    assertThat(deleted).isTrue();
    assertThat(groupRepository.isUserInGroup(user, created.id())).isFalse();
  }

  @Test
  void deleteUser_returns_false_when_missing() {
    val deleted =
        groupRepository.deleteUser(
            UserId.fromUuid(UUID.randomUUID()), GroupId.fromUuid(UUID.randomUUID()));

    assertThat(deleted).isFalse();
  }

  @Test
  void findUserIdsByGroupId_returns_all_users() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user1 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user2 = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val created = createGroup("group-" + FakeGenerator.username(), "desc", owner);
    groupRepository.addUser(user1, created.id());
    groupRepository.addUser(user2, created.id());

    val users = groupRepository.findUserIdsByGroupId(created.id());

    assertThat(users).contains(user1, user2);
  }

  @Test
  void findByUserId_returns_groups_for_user() {
    val owner = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val user = insertUser(FakeGenerator.username(), FakeGenerator.email());
    val groupA = createGroup("group-" + FakeGenerator.username(), "desc", owner);
    val groupB = createGroup("group-" + FakeGenerator.username(), "desc", owner);
    groupRepository.addUser(user, groupA.id());
    groupRepository.addUser(user, groupB.id());

    val page = groupRepository.findByUserId(user, PageRequest.of(0, 10));

    assertThat(page.getContent()).extracting(GroupEntity::id).contains(groupA.id(), groupB.id());
  }

  @Test
  void addUser_throws_when_group_missing() {
    val user = insertUser(FakeGenerator.username(), FakeGenerator.email());

    assertThatThrownBy(() -> groupRepository.addUser(user, GroupId.fromUuid(UUID.randomUUID())))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
