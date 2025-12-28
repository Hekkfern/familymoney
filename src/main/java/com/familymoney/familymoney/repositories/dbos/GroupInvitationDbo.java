package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupInvitationDbo(
    UUID id, GroupId groupId, GroupInvitationToken token, Instant createdAt, Instant expiresAt) {}
