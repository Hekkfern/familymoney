package com.familymoney.repositories.mappers;

import com.familymoney.generated.tables.GroupInvitations;
import com.familymoney.repositories.entities.GroupInvitationEntity;
import com.familymoney.types.GroupId;
import com.familymoney.types.GroupInvitationToken;
import java.time.OffsetDateTime;
import org.jooq.Record;

public final class GroupInvitationJooqMapper {

  private GroupInvitationJooqMapper() {}

  public static GroupInvitationEntity toEntity(final Record r) {
    OffsetDateTime createdAt = r.get(GroupInvitations.GROUP_INVITATIONS.CREATED_AT);
    OffsetDateTime expiresAt = r.get(GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT);

    return GroupInvitationEntity.builder()
        .id(r.get(GroupInvitations.GROUP_INVITATIONS.ID))
        .groupId(GroupId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.GROUP_ID)))
        .token(GroupInvitationToken.fromString(r.get(GroupInvitations.GROUP_INVITATIONS.TOKEN)))
        .createdAt(createdAt != null ? createdAt.toInstant() : null)
        .expiresAt(expiresAt != null ? expiresAt.toInstant() : null)
        .build();
  }
}
