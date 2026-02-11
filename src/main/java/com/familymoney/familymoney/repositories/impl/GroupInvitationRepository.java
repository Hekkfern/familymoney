package com.familymoney.familymoney.repositories.impl;

import com.familymoney.familymoney.generated.tables.GroupInvitations;
import com.familymoney.familymoney.repositories.IGroupInvitationRepository;
import com.familymoney.familymoney.repositories.dbos.GroupInvitationDbo;
import com.familymoney.familymoney.repositories.mappers.GroupInvitationJooqMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupInvitationRepository implements IGroupInvitationRepository {

  private final DSLContext db;

  @Override
  public Optional<GroupInvitationDbo> create(
      final GroupId groupId, final GroupInvitationToken token, final Instant expiresAt) {
    return db.insertInto(GroupInvitations.GROUP_INVITATIONS)
        .columns(
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .values(groupId.value(), token.value(), OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
        .returning(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toDbo);
  }

  @Override
  public Optional<GroupInvitationDbo> findByToken(final GroupInvitationToken token) {
    return db.select(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .from(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toDbo);
  }

  @Override
  public boolean deleteByToken(GroupInvitationToken token) {
    val rowsAffected =
        db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
            .where(GroupInvitations.GROUP_INVITATIONS.TOKEN.eq(token.value()))
            .execute();
    return rowsAffected > 0;
  }

  @Override
  public void deleteOlderThan(final Duration cutoff) {
    val threshold = OffsetDateTime.now(ZoneOffset.UTC).minusSeconds(cutoff.getSeconds());
    db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.CREATED_AT.lt(threshold))
        .execute();
  }
}
