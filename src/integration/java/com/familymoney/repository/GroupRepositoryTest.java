package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.repositories.GroupRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupDto;
import com.familymoney.domains.transactions.repositories.dtos.UpdateGroupDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupEntity;
import com.familymoney.domains.transactions.repositories.entitites.UserGroupEntity;
import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.repository.utils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
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
  private DatabaseCrud databaseCrud;

  @BeforeEach
  void setUp() {
    this.groupRepository = new GroupRepository(dslContext);
    this.databaseCrud = new DatabaseCrud(dslContext);
  }

  private UserId insertRandomUser() {
    final UserId userId = UserId.generate();
    final Instant now = Instant.now();
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

  @Nested
  class Create {

    @Test
    void persists_group_record() {
      final CurrencyUnit currency = Monetary.getCurrency("USD");
      final GroupName groupName = GroupName.fromString(FakeGenerator.groupName());
      final GroupId groupId = GroupId.generate();
      final Description description = Description.of("desc");
      final Instant now = Instant.now();

      final Optional<GroupEntity> groupOpt =
          groupRepository.create(new CreateGroupDto(groupId, groupName, description, currency));

      assertThat(groupOpt).isPresent();
      final GroupEntity group = groupOpt.get();
      assertThat(group.id()).isNotNull();
      assertThat(group.name()).isEqualTo(groupName);
      assertThat(group.description()).isEqualTo(description);
      assertThat(group.currency()).isEqualTo(currency);
      assertThat(group.createdAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(group.updatedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
    }

    @Test
    void throws_when_group_id_already_exists() {
      final GroupId groupId = insertRandomGroup();
      final CreateGroupDto dto =
          new CreateGroupDto(
              groupId,
              GroupName.fromString(FakeGenerator.groupName()),
              Description.of("desc"),
              Monetary.getCurrency("USD"));

      assertThatThrownBy(() -> groupRepository.create(dto))
          .isInstanceOf(DuplicateKeyException.class);
    }
  }

  @Nested
  class UpdateById {

    @Test
    void returns_true_when_everything_is_updated() {
      final GroupId groupId = insertRandomGroup();
      final Instant now = Instant.now();

      final GroupName newGroupName = GroupName.fromString(FakeGenerator.groupName());
      final Description newDescription = Description.of("new description");
      final UpdateGroupDto dataToUpdate = new UpdateGroupDto(newGroupName, newDescription);
      final boolean updated = groupRepository.updateById(groupId, dataToUpdate);

      assertThat(updated).isTrue();
      final GroupEntity found = groupRepository.findById(groupId).orElseThrow();
      assertThat(found.id()).isEqualTo(groupId);
      assertThat(found.name()).isEqualTo(newGroupName);
      assertThat(found.description()).isEqualTo(newDescription);
      assertThat(found.currency()).isEqualTo(Monetary.getCurrency("USD"));
      assertThat(found.createdAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
      assertThat(found.updatedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));
    }

    @Test
    void returns_true_when_only_name_is_updated() {
      final GroupId groupId = insertRandomGroup();
      final GroupEntity original = groupRepository.findById(groupId).orElseThrow();
      final GroupName newName = GroupName.fromString(FakeGenerator.groupName());
      final UpdateGroupDto dataToUpdate = UpdateGroupDto.builder().name(newName).build();

      final boolean updated = groupRepository.updateById(groupId, dataToUpdate);

      assertThat(updated).isTrue();
      final GroupEntity found = groupRepository.findById(groupId).orElseThrow();
      assertThat(found.name()).isEqualTo(newName);
      assertThat(found.description()).isEqualTo(original.description());
    }

    @Test
    void returns_true_when_only_description_is_updated() {
      final GroupId groupId = insertRandomGroup();
      final GroupEntity original = groupRepository.findById(groupId).orElseThrow();
      final Description newDescription = Description.of("only-desc-changed");
      final UpdateGroupDto dataToUpdate =
          UpdateGroupDto.builder().description(newDescription).build();

      final boolean updated = groupRepository.updateById(groupId, dataToUpdate);

      assertThat(updated).isTrue();
      final GroupEntity found = groupRepository.findById(groupId).orElseThrow();
      assertThat(found.description()).isEqualTo(newDescription);
      assertThat(found.name()).isEqualTo(original.name());
    }

    @Test
    void keeps_existing_values_when_all_fields_are_null() {
      final GroupId groupId = insertRandomGroup();
      final GroupEntity original = groupRepository.findById(groupId).orElseThrow();
      final UpdateGroupDto dataToUpdate = UpdateGroupDto.builder().build();

      final boolean updated = groupRepository.updateById(groupId, dataToUpdate);

      assertThat(updated).isTrue();
      final GroupEntity found = groupRepository.findById(groupId).orElseThrow();
      assertThat(found.name()).isEqualTo(original.name());
      assertThat(found.description()).isEqualTo(original.description());
    }

    @Test
    void returns_false_when_group_does_not_exist() {
      final Description newDescription = Description.of("new-desc");
      final GroupId groupId = GroupId.generate();
      final UpdateGroupDto dataToUpdate =
          UpdateGroupDto.builder().description(newDescription).build();

      final boolean updated = groupRepository.updateById(groupId, dataToUpdate);

      assertThat(updated).isFalse();
    }
  }

  @Nested
  class DeleteById {

    @Test
    void returns_true_when_group_exists() {
      final GroupId groupId = insertRandomGroup();

      final boolean deleted = groupRepository.deleteById(groupId);

      assertThat(deleted).isTrue();
      assertThat(groupRepository.findById(groupId)).isEmpty();
    }

    @Test
    void returns_false_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();

      final boolean deleted = groupRepository.deleteById(groupId);

      assertThat(deleted).isFalse();
    }
  }

  @Nested
  class FindByUserId {

    @Test
    void returns_groups_for_user_when_it_succeeds() {
      final UserId userId = insertRandomUser();
      final GroupId groupIdA = insertRandomGroup();
      final GroupId groupIdB = insertRandomGroup();
      groupRepository.addUser(userId, groupIdA);
      groupRepository.addUser(userId, groupIdB);

      final Page<GroupEntity> page = groupRepository.findByUserId(userId, PageRequest.of(0, 10));

      assertThat(page.getContent())
          .extracting(GroupEntity::id)
          .containsExactlyInAnyOrder(groupIdA, groupIdB);
    }

    @Test
    void returns_empty_page_when_user_has_no_groups() {
      final UserId userId = insertRandomUser();

      final Page<GroupEntity> page = groupRepository.findByUserId(userId, PageRequest.of(0, 10));

      assertThat(page.getContent()).isEmpty();
    }

    @Test
    void returns_second_page_when_results_exceed_page_size() {
      final UserId userId = insertRandomUser();
      final GroupId groupIdA = insertRandomGroup();
      final GroupId groupIdB = insertRandomGroup();
      final GroupId groupIdC = insertRandomGroup();
      groupRepository.addUser(userId, groupIdA);
      groupRepository.addUser(userId, groupIdB);
      groupRepository.addUser(userId, groupIdC);

      final Page<GroupEntity> page = groupRepository.findByUserId(userId, PageRequest.of(1, 2));

      assertThat(page.getContent()).hasSize(1);
      assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void returns_empty_page_when_user_does_not_exist() {
      final UserId userId = UserId.generate();

      final Page<GroupEntity> page = groupRepository.findByUserId(userId, PageRequest.of(0, 10));

      assertThat(page.getContent()).isEmpty();
    }
  }

  @Nested
  class FindById {

    @Test
    void returns_group_when_it_exists() {
      final GroupId groupId = insertRandomGroup();

      final Optional<GroupEntity> foundGroupOpt = groupRepository.findById(groupId);

      assertThat(foundGroupOpt).isPresent();
      final GroupEntity foundGroup = foundGroupOpt.get();
      assertThat(foundGroup.id()).isEqualTo(groupId);
    }

    @Test
    void returns_empty_when_it_does_not_exist() {
      final Optional<GroupEntity> found =
          groupRepository.findById(GroupId.fromUuid(UUID.randomUUID()));

      assertThat(found).isEmpty();
    }
  }

  @Nested
  class ExistsById {

    @Test
    void returns_true_when_group_exists() {
      final GroupId groupId = insertRandomGroup();

      assertThat(groupRepository.existsById(groupId)).isTrue();
    }

    @Test
    void returns_false_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();

      assertThat(groupRepository.existsById(groupId)).isFalse();
    }
  }

  @Nested
  class FindUserIdsByGroupId {

    @Test
    void returns_all_users_in_group() {
      final UserId userId1 = insertRandomUser();
      final UserId userId2 = insertRandomUser();
      insertRandomUser(); // noise user that should not be returned
      final GroupId groupId = insertRandomGroup();
      groupRepository.addUser(userId1, groupId);
      groupRepository.addUser(userId2, groupId);

      final List<UserId> users = groupRepository.findUserIdsByGroupId(groupId);

      assertThat(users).containsExactlyInAnyOrder(userId1, userId2);
    }

    @Test
    void returns_empty_list_when_group_has_no_members() {
      final GroupId groupId = insertRandomGroup();

      final List<UserId> users = groupRepository.findUserIdsByGroupId(groupId);

      assertThat(users).isEmpty();
    }

    @Test
    void returns_empty_list_when_group_does_not_exist() {
      final GroupId groupId = GroupId.generate();

      final List<UserId> users = groupRepository.findUserIdsByGroupId(groupId);

      assertThat(users).isEmpty();
    }
  }

  @Nested
  class IsUserInGroup {

    @Test
    void returns_true_when_user_is_in_group() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = insertRandomGroup();
      groupRepository.addUser(userId, groupId);

      final boolean result = groupRepository.isUserInGroup(userId, groupId);

      assertThat(result).isTrue();
    }

    @Test
    void returns_false_when_user_is_not_in_group() {
      final UserId user = insertRandomUser();
      final GroupId groupId = insertRandomGroup();

      final boolean result = groupRepository.isUserInGroup(user, groupId);

      assertThat(result).isFalse();
    }

    @Test
    void returns_false_when_group_does_not_exist() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = GroupId.generate();

      final boolean result = groupRepository.isUserInGroup(userId, groupId);

      assertThat(result).isFalse();
    }
  }

  @Nested
  class AddUser {

    @Test
    void adds_membership_and_queries_see_it() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = insertRandomGroup();
      final Instant now = Instant.now();

      final Optional<UserGroupEntity> addedOpt = groupRepository.addUser(userId, groupId);

      assertThat(addedOpt).isPresent();
      final UserGroupEntity added = addedOpt.get();
      assertThat(added.userId()).isEqualTo(userId);
      assertThat(added.groupId()).isEqualTo(groupId);
      assertThat(added.joinedAt()).isNotNull().isBetween(now.minusSeconds(1), now.plusSeconds(1));

      assertThat(groupRepository.isUserInGroup(userId, groupId)).isTrue();
      assertThat(groupRepository.findUserIdsByGroupId(groupId)).containsExactlyInAnyOrder(userId);
      final Page<GroupEntity> page = groupRepository.findByUserId(userId, PageRequest.of(0, 10));
      assertThat(page.getContent()).extracting(GroupEntity::id).contains(groupId);
    }

    @Test
    void throws_when_user_does_not_exist() {
      final UserId userId = UserId.generate();
      final GroupId groupId = insertRandomGroup();

      assertThatThrownBy(() -> groupRepository.addUser(userId, groupId))
          .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void throws_when_user_is_already_in_group() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = insertRandomGroup();
      groupRepository.addUser(userId, groupId);

      assertThatThrownBy(() -> groupRepository.addUser(userId, groupId))
          .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void throws_when_group_does_not_exist() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = GroupId.generate();

      assertThatThrownBy(() -> groupRepository.addUser(userId, groupId))
          .isInstanceOf(DataIntegrityViolationException.class);
    }
  }

  @Nested
  class DeleteUser {

    @Test
    void removes_membership() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = insertRandomGroup();
      groupRepository.addUser(userId, groupId);

      final boolean deleted = groupRepository.deleteUser(userId, groupId);

      assertThat(deleted).isTrue();
      assertThat(groupRepository.isUserInGroup(userId, groupId)).isFalse();
    }

    @Test
    void returns_false_when_user_does_not_exist() {
      final UserId userId = UserId.generate();
      final GroupId groupId = GroupId.generate();

      final boolean deleted = groupRepository.deleteUser(userId, groupId);

      assertThat(deleted).isFalse();
    }

    @Test
    void returns_false_when_group_does_not_exist() {
      final UserId userId = insertRandomUser();
      final GroupId groupId = GroupId.generate();

      final boolean deleted = groupRepository.deleteUser(userId, groupId);

      assertThat(deleted).isFalse();
    }
  }
}
