package com.familymoney.domains.transactions.repositories;

import static com.familymoney.config.Constants.DEFAULT_TIMEZONE_OFFSET;

import com.familymoney.domains.transactions.repositories.dbos.CreateGroupInvitationDbo;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.mappers.GroupInvitationJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.exceptions.UserNotFoundException;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.Keys;
import com.familymoney.generated.tables.GroupInvitations;
import com.familymoney.generated.tables.Groups;
import com.familymoney.security.OpaqueTokenHasher;
import com.familymoney.utils.ConstraintViolationUtils;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupInvitationRepository implements GroupInvitationRepository {

  private final DSLContext db;
  private final OpaqueTokenHasher tokenHasher;

  @Override
  public void create(final CreateGroupInvitationDbo dbo) {
    try {
      db.insertInto(GroupInvitations.GROUP_INVITATIONS)
          .columns(
              GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
              GroupInvitations.GROUP_INVITATIONS.USER_ID,
              GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH,
              GroupInvitations.GROUP_INVITATIONS.CREATED_BY,
              GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT,
              GroupInvitations.GROUP_INVITATIONS.LAST_SENT_AT)
          .values(
              dbo.groupId().value(),
              dbo.userId().value(),
              tokenHasher.hash(dbo.token().value()),
              dbo.createdBy().value(),
              dbo.expiresAt().toOffsetDateTime(),
              dbo.lastSentAt().atOffset(DEFAULT_TIMEZONE_OFFSET))
          .execute();
    } catch (final DataIntegrityViolationException exception) {
      if (ConstraintViolationUtils.isConstraintViolated(
          exception, Keys.GROUPS__GROUPS_CREATED_BY_FKEY)) {
        final String msg = "User with ID '%s' does not exist".formatted(dbo.createdBy().value());
        log.info(msg);
        throw new UserNotFoundException(msg, exception);
      }
      throw exception;
    }
  }

  @Override
  public Optional<GroupInvitationEntity> findByToken(final GroupInvitationToken token) {
    return db.select(
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
            GroupInvitations.GROUP_INVITATIONS.CREATED_BY,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT,
            GroupInvitations.GROUP_INVITATIONS.LAST_SENT_AT)
        .from(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toEntity);
  }

  @Override
  public Optional<GroupInvitationEntity> findByKey(
      final UserId userId, final GroupId groupId, final UserId createdBy) {
    return db.select(
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
            GroupInvitations.GROUP_INVITATIONS.CREATED_BY,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT,
            GroupInvitations.GROUP_INVITATIONS.LAST_SENT_AT)
        .from(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.USER_ID.eq(userId.value()))
        .and(GroupInvitations.GROUP_INVITATIONS.GROUP_ID.eq(groupId.value()))
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toEntity);
  }

  @Override
  public void deleteByToken(GroupInvitationToken token) {
    final int rowsAffected =
        db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
            .where(
                GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
            .execute();
    if (rowsAffected != 1) {
      log.warn("Could not delete group invitation with token '{}'", token.value());
    }
  }

  @Override
  public void deleteByKey(final UserId userId, final GroupId groupId, final UserId createdBy) {
    final int rowsAffected =
        db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
            .where(GroupInvitations.GROUP_INVITATIONS.USER_ID.eq(userId.value()))
            .and(GroupInvitations.GROUP_INVITATIONS.GROUP_ID.eq(groupId.value()))
            .and(GroupInvitations.GROUP_INVITATIONS.CREATED_BY.eq(createdBy.value()))
            .execute();
    if (rowsAffected != 1) {
      log.warn(
          "Could not delete group invitation with user ID '{}', group ID '{}', created by '{}'",
          userId.value(),
          groupId.value(),
          createdBy.value());
    }
  }

  @Override
  public int countByCreatedBy(UserId createdBy) {
    return db.fetchCount(
        db.selectOne()
            .from(GroupInvitations.GROUP_INVITATIONS)
            .where(GroupInvitations.GROUP_INVITATIONS.CREATED_BY.eq(createdBy.value()))
            .and(GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT.gt(DSL.currentOffsetDateTime())));
  }

  @Override
  public boolean existsByGroupIdAndUserId(final GroupId groupId, final UserId userId) {
    return db.fetchExists(
        db.selectOne()
            .from(GroupInvitations.GROUP_INVITATIONS)
            .where(GroupInvitations.GROUP_INVITATIONS.GROUP_ID.eq(groupId.value()))
            .and(GroupInvitations.GROUP_INVITATIONS.USER_ID.eq(userId.value()))
            .and(GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT.gt(DSL.currentOffsetDateTime())));
  }

  @Override
  public int deleteInvitationsOfPurgeableGroups(final Instant deletedBefore, final int batchSize) {
    return db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
        .where(
            DSL.row(
                    GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
                    GroupInvitations.GROUP_INVITATIONS.USER_ID,
                    GroupInvitations.GROUP_INVITATIONS.CREATED_BY)
                .in(
                    db.select(
                            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
                            GroupInvitations.GROUP_INVITATIONS.USER_ID,
                            GroupInvitations.GROUP_INVITATIONS.CREATED_BY)
                        .from(GroupInvitations.GROUP_INVITATIONS)
                        .join(Groups.GROUPS)
                        .on(Groups.GROUPS.ID.eq(GroupInvitations.GROUP_INVITATIONS.GROUP_ID))
                        .where(
                            Groups.GROUPS.DELETED_AT.lt(
                                deletedBefore.atOffset(DEFAULT_TIMEZONE_OFFSET)))
                        .limit(batchSize)))
        .execute();
  }
}
