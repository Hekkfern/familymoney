package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupInvitationEntity(
    UUID id, GroupId groupId, GroupInvitationToken token, Instant createdAt, Instant expiresAt) {}
