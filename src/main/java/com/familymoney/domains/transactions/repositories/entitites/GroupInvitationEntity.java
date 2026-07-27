package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;
import java.util.UUID;

public record GroupInvitationEntity(
    UUID id,
    GroupId groupId,
    UserId userId,
    GroupInvitationToken token,
    Instant createdAt,
    ExpirationTime expiresAt) {}
