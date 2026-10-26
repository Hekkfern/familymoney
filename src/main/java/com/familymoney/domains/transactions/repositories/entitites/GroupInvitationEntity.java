package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;

public record GroupInvitationEntity(
    GroupId groupId,
    UserId userId,
    UserId createdBy,
    ExpirationTime expiresAt,
    Instant lastSentAt) {}
