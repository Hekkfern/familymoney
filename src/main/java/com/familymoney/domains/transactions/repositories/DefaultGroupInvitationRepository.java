package com.familymoney.domains.transactions.repositories;

import com.familymoney.domains.transactions.repositories.dtos.CreateGroupInvitationDto;
import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.repositories.mappers.GroupInvitationJooqMapper;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupInvitations;
import com.familymoney.security.OpaqueTokenHasher;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DefaultGroupInvitationRepository implements GroupInvitationRepository {

  private final DSLContext db;
  private final OpaqueTokenHasher tokenHasher;

  @Override
  public Optional<GroupInvitationEntity> create(final CreateGroupInvitationDto data) {
    return db.insertInto(GroupInvitations.GROUP_INVITATIONS)
        .columns(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
            GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .values(
            data.id(),
            data.groupId().value(),
            data.userId().value(),
            tokenHasher.hash(data.token().value()),
            data.expiresAt().toOffsetDateTime())
        .returning(
            GroupInvitations.GROUP_INVITATIONS.ID,
            GroupInvitations.GROUP_INVITATIONS.GROUP_ID,
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
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
            GroupInvitations.GROUP_INVITATIONS.USER_ID,
            GroupInvitations.GROUP_INVITATIONS.CREATED_AT,
            GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT)
        .from(GroupInvitations.GROUP_INVITATIONS)
        .where(GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
        .fetchOptional()
        .map(GroupInvitationJooqMapper::toEntity);
  }

  @Override
  public boolean deleteByToken(GroupInvitationToken token) {
    final int rowsAffected =
        db.deleteFrom(GroupInvitations.GROUP_INVITATIONS)
            .where(
                GroupInvitations.GROUP_INVITATIONS.TOKEN_HASH.eq(tokenHasher.hash(token.value())))
            .execute();
    return rowsAffected > 0;
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
}
