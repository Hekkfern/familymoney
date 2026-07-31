package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;
import com.familymoney.testutils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.money.Monetary;
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
class GroupInvitationRepositoryTest {

  @Container @ServiceConnection
  private static final PostgreSQLContainer postgresContainer =
      new PostgreSQLContainer(POSTGRESQL_CONTAINER_IMAGE);

  @Autowired private DSLContext dslContext;

  private GroupInvitationRepository groupInvitationRepository;

  @BeforeEach
  void setUp() {
    this.groupInvitationRepository = new GroupInvitationRepository(dslContext);
  }

  private UserId insertRandomUser() {
    final UserId userId = UserId.generate();
    final Instant now = Instant.ofEpochSecond(1778755330);
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
    final GroupId groupId = GroupId.generate();
    final Instant now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext,
        groupId,
        GroupName.fromString(FakeGenerator.groupName()),
        "desc",
        Monetary.getCurrency("USD"),
        now);
    return groupId;
  }

  // region IGroupInvitationRepository.create()

  @Test
  void create_persists_invitation_record() {
    final UserId userId = insertRandomUser();
    final Instant now = Instant.now();
    final GroupId groupId = insertRandomGroup();
    final GroupInvitationToken token = GroupInvitationToken.generate();
    final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(3600));

    final Optional<GroupInvitationEntity> invitationOpt =
        groupInvitationRepository.create(
            new CreateGroupInvitationDto(UUID.randomUUID(), groupId, userId, token, expiration));

    assertThat(invitationOpt).isPresent();
    final GroupInvitationEntity invitation = invitationOpt.get();
    assertThat(invitation.id()).isNotNull();
    assertThat(invitation.groupId()).isNotNull().isEqualTo(groupId);
    assertThat(invitation.userId()).isNotNull().isEqualTo(userId);
    assertThat(invitation.token()).isNotNull().isEqualTo(token);
    assertThat(invitation.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(invitation.expiresAt().value())
        .isNotNull()
        .isBetween(expiration.value().minusSeconds(1), expiration.value().plusSeconds(1));
  }

  @Test
  void create_throws_when_group_does_not_exist() {
    final UserId userId = insertRandomUser();
    final GroupId groupId = GroupId.generate();

    final CreateGroupInvitationDto dto =
        new CreateGroupInvitationDto(
            UUID.randomUUID(),
            groupId,
            userId,
            GroupInvitationToken.generate(),
            ExpirationTime.of(Instant.now().plusSeconds(300)));
    assertThatThrownBy(() -> groupInvitationRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_user_does_not_exist() {
    final GroupId groupId = insertRandomGroup();
    final UserId userId = UserId.generate();

    final CreateGroupInvitationDto dto =
        new CreateGroupInvitationDto(
            UUID.randomUUID(),
            groupId,
            userId,
            GroupInvitationToken.generate(),
            ExpirationTime.of(Instant.now().plusSeconds(300)));
    assertThatThrownBy(() -> groupInvitationRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    final UserId userId = insertRandomUser();
    final GroupId groupId = GroupId.generate();
    final Instant now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext,
        groupId,
        GroupName.fromString("group-" + FakeGenerator.username()),
        "desc",
        Monetary.getCurrency("USD"),
        now);
    final GroupInvitationToken token = GroupInvitationToken.generate();
    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        groupId,
        userId,
        token,
        now,
        ExpirationTime.of(now.plusSeconds(300)));

    final CreateGroupInvitationDto dto =
        new CreateGroupInvitationDto(
            UUID.randomUUID(), groupId, userId, token, ExpirationTime.of(now.plusSeconds(600)));
    assertThatThrownBy(() -> groupInvitationRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // endregion

  // region IGroupInvitationRepository.findByToken()

  @Test
  void findByToken_returns_invitation_when_it_exists() {
    final UserId userId = insertRandomUser();
    final GroupId groupId = GroupId.generate();
    final Instant now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext,
        groupId,
        GroupName.fromString("group-" + FakeGenerator.username()),
        "desc",
        Monetary.getCurrency("USD"),
        now);
    final GroupInvitationToken token = GroupInvitationToken.generate();
    final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(300));
    DatabaseCrud.insertGroupInvitation(
        dslContext, UUID.randomUUID(), groupId, userId, token, now, expiration);

    final Optional<GroupInvitationEntity> invitationFoundOpt =
        groupInvitationRepository.findByToken(token);

    assertThat(invitationFoundOpt).isPresent();
    final GroupInvitationEntity invitationFound = invitationFoundOpt.get();
    assertThat(invitationFound.id()).isNotNull();
    assertThat(invitationFound.groupId()).isEqualTo(groupId);
    assertThat(invitationFound.token()).isEqualTo(token);
    assertThat(invitationFound.createdAt()).isEqualTo(now);
    assertThat(invitationFound.expiresAt()).isEqualTo(expiration);
  }

  @Test
  void findByToken_returns_empty_when_token_does_not_exist() {
    final GroupInvitationToken token = GroupInvitationToken.generate();

    final Optional<GroupInvitationEntity> invitationFoundOpt =
        groupInvitationRepository.findByToken(token);

    assertThat(invitationFoundOpt).isEmpty();
  }

  // endregion

  // region IGroupInvitationRepository.deleteByToken()

  @Test
  void deleteByToken_deletes_invitation_when_it_exists() {
    final UserId userId = insertRandomUser();
    final GroupId groupId = insertRandomGroup();
    final Instant now = Instant.now();
    final GroupInvitationToken token = GroupInvitationToken.generate();
    final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(300));
    DatabaseCrud.insertGroupInvitation(
        dslContext, UUID.randomUUID(), groupId, userId, token, now, expiration);

    final boolean deleted = groupInvitationRepository.deleteByToken(token);

    assertThat(deleted).isTrue();
    assertThat(groupInvitationRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByToken_returns_false_when_token_does_not_exist() {
    final GroupInvitationToken token = GroupInvitationToken.generate();

    final boolean deleted = groupInvitationRepository.deleteByToken(token);

    assertThat(deleted).isFalse();
  }

  // endregion

  // region IGroupInvitationRepository.countByGroupIdAndUserId()

  @Test
  void countByGroupIdAndUserId_returns_count_when_there_are_entries() {
    final UserId userId = insertRandomUser();
    final UserId anotherUserId = insertRandomUser();
    final GroupId groupId = insertRandomGroup();
    final GroupId anotherGroupId = insertRandomGroup();
    final Instant now = Instant.now();
    final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(300));

    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        groupId,
        userId,
        GroupInvitationToken.generate(),
        now,
        expiration);
    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        groupId,
        userId,
        GroupInvitationToken.generate(),
        now.plusSeconds(1),
        ExpirationTime.of(expiration.value().plusSeconds(1)));
    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        groupId,
        anotherUserId,
        GroupInvitationToken.generate(),
        now,
        expiration);
    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        anotherGroupId,
        userId,
        GroupInvitationToken.generate(),
        now,
        expiration);

    final long invitationCount = groupInvitationRepository.countByGroupIdAndUserId(groupId, userId);

    assertThat(invitationCount).isEqualTo(2L);
  }

  @Test
  void countByGroupIdAndUserId_returns_zero_when_there_are_no_entries() {
    final UserId userId = insertRandomUser();
    final UserId anotherUserId = insertRandomUser();
    final GroupId groupId = insertRandomGroup();
    final GroupId anotherGroupId = insertRandomGroup();
    final Instant now = Instant.now();
    final ExpirationTime expiration = ExpirationTime.of(now.plusSeconds(300));

    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        groupId,
        anotherUserId,
        GroupInvitationToken.generate(),
        now,
        expiration);
    DatabaseCrud.insertGroupInvitation(
        dslContext,
        UUID.randomUUID(),
        anotherGroupId,
        userId,
        GroupInvitationToken.generate(),
        now,
        expiration);

    final long invitationCount = groupInvitationRepository.countByGroupIdAndUserId(groupId, userId);

    assertThat(invitationCount).isZero();
  }

  // endregion
}
