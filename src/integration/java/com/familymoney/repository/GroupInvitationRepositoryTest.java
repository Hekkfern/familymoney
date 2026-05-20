package com.familymoney.repository;

import static com.familymoney.testutils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.familymoney.domains.transactions.repositories.GroupInvitationRepository;
import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.user.types.Email;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.domains.user.types.UserName;
import com.familymoney.testutils.DatabaseCrud;
import com.familymoney.testutils.FakeGenerator;
import java.time.Instant;
import java.util.UUID;
import lombok.val;
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
    val now = Instant.ofEpochSecond(1778454330);
    DatabaseCrud.insertGroup(
        dslContext, groupId, "group-" + FakeGenerator.username(), "desc", "USD", now);
    return groupId;
  }

  // region IGroupInvitationRepository.create()

  @Test
  void create_persists_invitation_record() {
    val userId = insertRandomUser();
    val now = Instant.now();
    val groupId = insertRandomGroup();
    val token = GroupInvitationToken.generate();
    val expiration = now.plusSeconds(3600);

    val invitationOpt =
        groupInvitationRepository.create(
            new CreateGroupInvitationDto(UUID.randomUUID(), groupId, userId, token, expiration));

    assertThat(invitationOpt).isPresent();
    val invitation = invitationOpt.get();
    assertThat(invitation.id()).isNotNull();
    assertThat(invitation.groupId()).isNotNull().isEqualTo(groupId);
    assertThat(invitation.userId()).isNotNull().isEqualTo(userId);
    assertThat(invitation.token()).isNotNull().isEqualTo(token);
    assertThat(invitation.createdAt())
        .isNotNull()
        .isBetween(now.minusSeconds(1), now.plusSeconds(1));
    assertThat(invitation.expiresAt())
        .isNotNull()
        .isBetween(expiration.minusSeconds(1), expiration.plusSeconds(1));
  }

  @Test
  void create_throws_when_group_does_not_exist() {
    val userId = insertRandomUser();
    val groupId = GroupId.generate();

    val dto =
        new CreateGroupInvitationDto(
            UUID.randomUUID(),
            groupId,
            userId,
            GroupInvitationToken.generate(),
            Instant.now().plusSeconds(300));
    assertThatThrownBy(() -> groupInvitationRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val userId = insertRandomUser();
    val groupId = GroupId.generate();
    val now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext, groupId, "group-" + FakeGenerator.username(), "desc", "USD", now);
    val token = GroupInvitationToken.generate();
    DatabaseCrud.insertGroupInvitation(
        dslContext, UUID.randomUUID(), groupId, userId, token, now, now.plusSeconds(300));

    val dto =
        new CreateGroupInvitationDto(
            UUID.randomUUID(), groupId, userId, token, now.plusSeconds(600));
    assertThatThrownBy(() -> groupInvitationRepository.create(dto))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  // endregion

  // region IGroupInvitationRepository.findByToken()

  @Test
  void findByToken_returns_invitation_when_exists() {
    val userId = insertRandomUser();
    val groupId = GroupId.generate();
    val now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext, groupId, "group-" + FakeGenerator.username(), "desc", "USD", now);
    val token = GroupInvitationToken.generate();
    val expiration = now.plusSeconds(300);
    DatabaseCrud.insertGroupInvitation(
        dslContext, UUID.randomUUID(), groupId, userId, token, now, expiration);

    val invitationFoundOpt = groupInvitationRepository.findByToken(token);

    assertThat(invitationFoundOpt).isPresent();
    val invitationFound = invitationFoundOpt.get();
    assertThat(invitationFound.id()).isNotNull();
    assertThat(invitationFound.groupId()).isEqualTo(groupId);
    assertThat(invitationFound.token()).isEqualTo(token);
    assertThat(invitationFound.createdAt()).isEqualTo(now);
    assertThat(invitationFound.expiresAt()).isEqualTo(expiration);
  }

  @Test
  void findByToken_returns_empty_when_missing() {
    val invitationFoundOpt = groupInvitationRepository.findByToken(GroupInvitationToken.generate());

    assertThat(invitationFoundOpt).isEmpty();
  }

  // endregion

  // region IGroupInvitationRepository.deleteByToken()

  @Test
  void deleteByToken_deletes_invitation() {
    val userId = insertRandomUser();
    val groupId = GroupId.generate();
    val now = Instant.now();
    DatabaseCrud.insertGroup(
        dslContext, groupId, "group-" + FakeGenerator.username(), "desc", "USD", now);
    val token = GroupInvitationToken.generate();
    val expiration = now.plusSeconds(300);
    DatabaseCrud.insertGroupInvitation(
        dslContext, UUID.randomUUID(), groupId, userId, token, now, expiration);

    val deleted = groupInvitationRepository.deleteByToken(token);

    assertThat(deleted).isTrue();
    assertThat(groupInvitationRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByToken_returns_false_when_missing() {
    val deleted = groupInvitationRepository.deleteByToken(GroupInvitationToken.generate());

    assertThat(deleted).isFalse();
  }

  // endregion

  // region IGroupInvitationRepository.countByGroupIdAndUserId()

  @Test
  void countByGroupIdAndUserId_returns_count_when_there_are_entries() {
    val userId = insertRandomUser();
    val anotherUserId = insertRandomUser();
    val groupId = insertRandomGroup();
    val anotherGroupId = insertRandomGroup();
    val now = Instant.now();
    val expiration = now.plusSeconds(300);

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
        expiration.plusSeconds(1));
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

    val invitationCount = groupInvitationRepository.countByGroupIdAndUserId(groupId, userId);

    assertThat(invitationCount).isEqualTo(2L);
  }

  @Test
  void countByGroupIdAndUserId_returns_zero_when_there_are_no_entries() {
    val userId = insertRandomUser();
    val anotherUserId = insertRandomUser();
    val groupId = insertRandomGroup();
    val anotherGroupId = insertRandomGroup();
    val now = Instant.now();
    val expiration = now.plusSeconds(300);

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

    val invitationCount = groupInvitationRepository.countByGroupIdAndUserId(groupId, userId);

    assertThat(invitationCount).isZero();
  }

  // endregion
}
