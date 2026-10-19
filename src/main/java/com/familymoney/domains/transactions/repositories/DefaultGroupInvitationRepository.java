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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DefaultGroupInvitationRepository implements GroupInvitationRepository {

  private final DSLContext db;
  private final OpaqueTokenHasher tokenHasher;

  @Override
  public void create(final CreateGroupInvitationDbo dto) {
    try {
      db.insertInto(GroupInvitations.GROUP_INVITATIONS)
          .columns(
              GroupInvitations.GROUP_INVITATIONS.ID,
              GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
              GroupInvitations.GROUP_INVITATIONS.USER_ID,
              GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH,
              GroupInvitations.GROUP_INVITATIONS.CREATED_BY,
              GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
          .values(
              dto.id(),
              dto.groupId().value(),
              dto.userId().value(),
              tokenHasher.hash(dto.token().value()),
              dto.createdBy().value(),
              dto.expiresAt().toOffsetDateTime())
          .execute();
    } catch (final DataIntegrityViolationException exception) {
      if (ConstraintViolationUtils.isConstraintViolated(
          exception, Keys.GROUPS__GROUPS_CREATED_BY_FKEY)) {
        final String msg = "User with ID '%s' does not exist".formatted(dto.createdBy().value());
        log.info(msg);
        throw new UserNotFoundException(msg, exception);
      }
      throw exception;
    }
  }

  @Override
  public Optional<GroupInvitationEntity> findByToken(final GroupInvitationToken token) {
    return db.select(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .from(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
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
      log.warn("Could not delete group invitation with ID: {}", token.value());
    }
  }

  @Override
  public long countByGroupIdAndUserId(final GroupId groupId, final UserId userId) {
    return db.fetchCount(
        GroupInvitations.GROUP_INVITATIONS,
        GroupInvitations.GROUP_INVITATIONS
            .GROUP_ID
            .eq(groupId.value())
            .and(GroupInvitations.GROUP_INVITATIONS.USER_ID.eq(userId.value())));
  }

  @Override
  public int deleteInvitationsOfPurgeableGroups(final Instant deletedBefore, final int batchSize) {
    return db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
        .where(
            GroupInvitations.GROUP_INVITATIONS.ID.in(
                db.select(GroupInvitations.GROUP_INVITATIONS.ID)
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
