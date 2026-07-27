package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.generated.tables.GroupInvitations;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.jooq.Record;

public final class GroupInvitationJooqMapper {

  private GroupInvitationJooqMapper() {
    /* this class is not intended to be instantiated */
  }

  public static GroupInvitationEntity toEntity(final Record r) {
    OffsetDateTime createdAt =
        Objects.requireNonNull(r.get(GroupInvitations.GROUP_INVITATIONS.CREATED_AT));
    OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT));

    return new GroupInvitationEntity(
        r.get(GroupInvitations.GROUP_INVITATIONS.ID),
        GroupId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.GROUP_ID)),
        UserId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.USER_ID)),
        GroupInvitationToken.fromString(r.get(GroupInvitations.GROUP_INVITATIONS.TOKEN)),
        createdAt.toInstant(),
        ExpirationTime.of(expiresAt));
  }
}
