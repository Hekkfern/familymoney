package com.familymoney.domains.transactions.repositories.mappers;

import com.familymoney.domains.transactions.repositories.entitites.GroupInvitationEntity;
import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
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
    final OffsetDateTime expiresAt =
        Objects.requireNonNull(r.get(GroupInvitations.GROUP_INVITATIONS.EXPIRES_AT));
    final OffsetDateTime lastSentAt =
        Objects.requireNonNull(r.get(GroupInvitations.GROUP_INVITATIONS.LAST_SENT_AT));

    return new GroupInvitationEntity(
        GroupId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.GROUP_ID)),
        UserId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.USER_ID)),
        UserId.fromUuid(r.get(GroupInvitations.GROUP_INVITATIONS.CREATED_BY)),
        ExpirationTime.of(expiresAt),
        lastSentAt.toInstant());
  }
}
