package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.mappers.GroupInvitationJooqMapper;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.generated.tables.GroupInvitations;
import java.time.Duration;
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
  public Optional<GroupInvitationEntity> create(final CreateGroupInvitationDto data) {
    return db.insertInto(GroupInvitations.GROUP_INVITATIONS)
        .columns(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .values(
            data.id(),
            data.groupId().value(),
            data.token().value(),
            OffsetDateTime.ofInstant(data.expiresAt(), ZoneOffset.UTC))
        .returning(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toEntity);
  }

  @Override
  public Optional<GroupInvitationEntity> findByToken(final GroupInvitationToken token) {
    return db.select(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN,
            GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .from(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.TOKEN.eq(token.value()))
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toEntity);
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
