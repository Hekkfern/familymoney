package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

/**
 * DTO for creating a new group invitation record in the database
 *
 * @param id the unique identifier for the group invitation record
 * @param groupId the identifier of the group for which the invitation is being created
 * @param token the unique token associated with the group invitation, used for validation and
 *     retrieval
 * @param expiresAt the timestamp indicating when the invitation expires, after which it should no
 *     longer be valid
 */
@Builder
public record CreateGroupInvitationDto(
    UUID id, GroupId groupId, UserId userId, GroupInvitationToken token, Instant expiresAt) {}
