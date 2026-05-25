package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.testutils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@JooqTest
@Testcontainers
class GroupRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private GroupRepository groupRepository;

  @BeforeEach
  void setUp() {
    this.groupRepository = new GroupRepository(dslContext);
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
    val now = Instant.ofEpochSecond(1778754330);
    DatabaseCrud.insertGroup(
        dslContext,
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        "desc",
        Monetary.getCurrency("USD"),
        now);
    return groupId;
  }

  // region IGroupRepository.create()

  @Test
  void create_persists_group_record() {
    val currency = Monetary.getCurrency("USD");
    val groupName = GroupName.fromString(FakeGenerator.groupName());
    val groupId = GroupId.generate();
    val now = Instant.now();

    val groupOpt = groupRepository.create(new CreateGroupDto(groupId, groupName, "desc", currency));

    assertThat(groupOpt).isPresent();
    val group = groupOpt.get();
    assertThat(group.id()).isNotNull();
    assertThat(group.name()).isEqualTo(groupName);
    assertThat(group.description()).isEqualTo("desc");
    assertThat(group.currency()).isEqualTo(currency);
    assertThat(group.createdAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(group.updatedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
  }

  // endregion

  // region IGroupRepository.updateById()

  @Test
  void updateById_updates_group_fields() {
    val groupId = insertRandomGroup();
    val now = Instant.now();

    val newGroupName = GroupName.fromString(FakeGenerator.groupName());
    val newDescription = "new description";
    val dataToUpdate =
        UpdateGroupDto.builder().name(newGroupName).description(newDescription).build();
    val updated = groupRepository.updateById(groupId, dataToUpdate);

    assertThat(updated).isTrue();
    val found = groupRepository.findById(groupId).orElseThrow();
    assertThat(found.id()).isEqualTo(groupId);
    assertThat(found.name()).isEqualTo(newGroupName);
    assertThat(found.description()).isEqualTo("new description");
    assertThat(found.currency()).isEqualTo(Monetary.getCurrency("USD"));
    assertThat(found.createdAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(found.updatedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
  }

  @Test
  void updateById_returns_false_when_missing() {
    val update = UpdateGroupDto.builder().description("new-desc").build();

    val updated = groupRepository.updateById(GroupId.fromUuid(UUID.randomUUID()), update);

    assertThat(updated).isFalse();
  }

  // endregion

  // region IGroupRepository.deleteById()

  @Test
  void deleteById_removes_group() {
    val groupId = insertRandomGroup();

    val deleted = groupRepository.deleteById(groupId);

    assertThat(deleted).isTrue();
    assertThat(groupRepository.findById(groupId)).isEmpty();
  }

  @Test
  void deleteById_returns_false_when_missing() {
    val groupId = GroupId.generate();

    val deleted = groupRepository.deleteById(groupId);

    assertThat(deleted).isFalse();
  }

  // endregion

  // region IGroupRepository.findByUserId()

  // TODO

  // endregion

  // region IGroupRepository.findById()

  @Test
  void findById_returns_group_when_exists() {
    val groupId = insertRandomGroup();

    val foundGroupOpt = groupRepository.findById(groupId);

    assertThat(foundGroupOpt).isPresent();
    val foundGroup = foundGroupOpt.get();
    assertThat(foundGroup.id()).isEqualTo(groupId);
  }

  @Test
  void findById_returns_empty_when_missing() {
    val found = groupRepository.findById(GroupId.fromUuid(UUID.randomUUID()));

    assertThat(found).isEmpty();
  }

  // endregion

  // region IGroupRepository.existsById()

  // TODO

  // endregion

  // region IGroupRepository.findUserIdsByGroupId()

  @Test
  void findUserIdsByGroupId_returns_all_users() {
    val userId1 = insertRandomUser();
    val userId2 = insertRandomUser();
    insertRandomUser(); // noise user that should not be returned
    val groupId = insertRandomGroup();
    groupRepository.addUser(userId1, groupId);
    groupRepository.addUser(userId2, groupId);

    val users = groupRepository.findUserIdsByGroupId(groupId);

    assertThat(users).containsExactlyInAnyOrder(userId1, userId2);
  }

  // endregion

  // region IGroupRepository.isUserInGroup()

  @Test
  void isUserInGroup_returns_true_when_user_is_in_group() {
    val userId = insertRandomUser();
    val groupId = insertRandomGroup();
    groupRepository.addUser(userId, groupId);

    val result = groupRepository.isUserInGroup(userId, groupId);

    assertThat(result).isTrue();
  }

  @Test
  void isUserInGroup_returns_false_when_user_is_not_in_group() {
    val user = insertRandomUser();
    val groupId = insertRandomGroup();

    val result = groupRepository.isUserInGroup(user, groupId);

    assertThat(result).isFalse();
  }

  // endregion

  // region IGroupRepository.addUser()

  @Test
  void addUser_adds_membership_and_queries_see_it() {
    val userId = insertRandomUser();
    val groupId = insertRandomGroup();
    val now = Instant.now();

    val addedOpt = groupRepository.addUser(userId, groupId);

    assertThat(addedOpt).isPresent();
    val added = addedOpt.get();
    assertThat(added.userId()).isEqualTo(userId);
    assertThat(added.groupId()).isEqualTo(groupId);
    assertThat(added.joinedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));

    assertThat(groupRepository.isUserInGroup(userId, groupId)).isTrue();
    assertThat(groupRepository.findUserIdsByGroupId(groupId)).containsExactlyInAnyOrder(userId);
    val page = groupRepository.findByUserId(userId, PageRequest.of(0, 10));
    assertThat(page.getContent()).extracting(GroupEntity::id).contains(groupId);
  }

  @Test
  void addUser_throws_when_duplicate() {
    val userId = insertRandomUser();
    val groupId = insertRandomGroup();
    groupRepository.addUser(userId, groupId);

    assertThatThrownBy(() -> groupRepository.addUser(userId, groupId))
        .isInstanceOf(DuplicateKeyException.class);
  }

  @Test
  void addUser_throws_when_group_missing() {
    val userId = insertRandomUser();
    val groupId = GroupId.generate();

    assertThatThrownBy(() -> groupRepository.addUser(userId, groupId))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // endregion

  // region IGroupRepository.deleteUser()

  @Test
  void deleteUser_removes_membership() {
    val userId = insertRandomUser();
    val groupId = insertRandomGroup();
    groupRepository.addUser(userId, groupId);

    val deleted = groupRepository.deleteUser(userId, groupId);

    assertThat(deleted).isTrue();
    assertThat(groupRepository.isUserInGroup(userId, groupId)).isFalse();
  }

  @Test
  void deleteUser_returns_false_when_user_does_not_exist() {
    val userId = UserId.generate();
    val groupId = GroupId.generate();

    val deleted = groupRepository.deleteUser(userId, groupId);

    assertThat(deleted).isFalse();
  }

  // endregion

  // region IGroupRepository.findByUserId()

  @Test
  void findByUserId_returns_groups_for_user() {
    val userId = insertRandomUser();
    val groupIdA = insertRandomGroup();
    val groupIdB = insertRandomGroup();
    groupRepository.addUser(userId, groupIdA);
    groupRepository.addUser(userId, groupIdB);

    val page = groupRepository.findByUserId(userId, PageRequest.of(0, 10));

    assertThat(page.getContent())
        .extracting(GroupEntity::id)
        .containsExactlyInAnyOrder(groupIdA, groupIdB);
  }

  // endregion
}
