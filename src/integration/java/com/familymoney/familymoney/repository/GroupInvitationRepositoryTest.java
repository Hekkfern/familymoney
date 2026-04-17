package com.familymoney.familymoney.repository;

import static com.familymoney.familymoney.utils.TestConstants.POSTGRESQL_CONTAINER_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import com.familymoney.familymoney.generated.tables.GroupInvitations;
import com.familymoney.familymoney.generated.tables.Groups;
import com.familymoney.familymoney.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.familymoney.repositories.entities.GroupInvitationEntity;
import com.familymoney.familymoney.repositories.impl.GroupInvitationRepository;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import com.familymoney.familymoney.utils.FakeGenerator;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

  private GroupInvitationEntity insertInvitation(
      final GroupId groupId, final GroupInvitationToken token, final OffsetDateTime createdAt) {
    val r =
        dslContext
            .insertInto(GroupInvitations.GROUP_INVITATIONS)
            .columns(
                GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
                GroupInvitations.GROUP_INVITATIONS.TOKEN,
                GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
                GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
            .values(groupId.value(), token.value(), createdAt, createdAt.plusDays(1))
            .returning(
                GroupInvitations.GROUP_INVITATIONS.ID,
                GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
                GroupInvitations.GROUP_INVITATIONS.TOKEN,
                GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
                GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
            .fetchOne();
    return GroupInvitationEntity.builder()
        .id(r.getId())
        .groupId(GroupId.fromUuid(r.getGroupId()))
        .token(GroupInvitationToken.fromString(r.getToken()))
        .createdAt(r.getCreatedAt().toInstant())
        .expiresAt(r.getExpiresAt().toInstant())
        .build();
  }

  @Test
  void create_persists_invitation() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val token = GroupInvitationToken.generate();
    val expiresAt = Instant.now().plusSeconds(3600);

    val created =
        groupInvitationRepository.create(
            new CreateGroupInvitationDto(any(), groupId, token, expiresAt));

    assertThat(created).isPresent();
    val dbo = created.get();
    assertThat(dbo.id()).isNotNull();
    assertThat(dbo.groupId()).isEqualTo(groupId);
    assertThat(dbo.token()).isEqualTo(token);
    assertThat(dbo.expiresAt()).isEqualTo(expiresAt);
  }

  @Test
  void create_throws_when_group_missing() {
    val missingGroup = GroupId.fromUuid(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                groupInvitationRepository.create(
                    new CreateGroupInvitationDto(
                        any(),
                        missingGroup,
                        GroupInvitationToken.generate(),
                        Instant.now().plusSeconds(300))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void create_throws_when_token_is_duplicate() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val token = GroupInvitationToken.generate();

    groupInvitationRepository.create(
        new CreateGroupInvitationDto(any(), groupId, token, Instant.now().plusSeconds(300)));

    assertThatThrownBy(
            () ->
                groupInvitationRepository.create(
                    new CreateGroupInvitationDto(
                        any(), groupId, token, Instant.now().plusSeconds(600))))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findByToken_returns_invitation_when_exists() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val token = GroupInvitationToken.generate();
    groupInvitationRepository.create(
        new CreateGroupInvitationDto(any(), groupId, token, Instant.now().plusSeconds(300)));

    val found = groupInvitationRepository.findByToken(token);

    assertThat(found).isPresent();
    assertThat(found.get().groupId()).isEqualTo(groupId);
    assertThat(found.get().token()).isEqualTo(token);
  }

  @Test
  void findByToken_returns_empty_when_missing() {
    val found = groupInvitationRepository.findByToken(GroupInvitationToken.generate());

    assertThat(found).isEmpty();
  }

  @Test
  void deleteByToken_deletes_invitation() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val token = GroupInvitationToken.generate();
    groupInvitationRepository.create(
        new CreateGroupInvitationDto(any(), groupId, token, Instant.now().plusSeconds(300)));

    val deleted = groupInvitationRepository.deleteByToken(token);

    assertThat(deleted).isTrue();
    assertThat(groupInvitationRepository.findByToken(token)).isEmpty();
  }

  @Test
  void deleteByToken_returns_false_when_missing() {
    val deleted = groupInvitationRepository.deleteByToken(GroupInvitationToken.generate());

    assertThat(deleted).isFalse();
  }

  @Test
  void deleteOlderThan_removes_only_old_invitations() {
    val groupId = insertGroup("group-" + FakeGenerator.username(), "desc", "USD");
    val now = OffsetDateTime.now(ZoneOffset.UTC);
    val oldToken = GroupInvitationToken.generate();
    val recentToken = GroupInvitationToken.generate();

    insertInvitation(groupId, oldToken, now.minusDays(2));
    insertInvitation(groupId, recentToken, now.minusMinutes(30));

    groupInvitationRepository.deleteOlderThan(Duration.ofDays(1));

    assertThat(groupInvitationRepository.findByToken(oldToken)).isEmpty();
    assertThat(groupInvitationRepository.findByToken(recentToken)).isPresent();
  }
}
