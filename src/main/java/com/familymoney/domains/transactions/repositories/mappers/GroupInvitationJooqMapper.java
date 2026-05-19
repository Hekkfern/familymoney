package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.user.types.UserId;
import com.familymoney.generated.tables.GroupInvitations;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class GroupInvitationJooqMapper {

  private GroupInvitationJooqMapper() {}

  public static GroupInvitationEntity toEntity(final Record r) {
    OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(GroupInvitations.GROUP_INVITATIONS.CREATED_AT));
    OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT));

    return GroupInvitationEntity.builder()
        .id(r.get(GroupInvitations.GROUP_INVITATIONS.ID))
        .groupId(GroupId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.GROUP_ID)))
        .userId(UserId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.USER_ID)))
        .token(GroupInvitationToken.fromString(r.get(GroupInvitations.GROUP_INVITATIONS.TOKEN)))
        .createdAt(createdAt.toInstant())
        .expiresAt(expiresAt.toInstant())
        .build();
  }
}
