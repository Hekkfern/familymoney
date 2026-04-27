package com.familymoney.repositories.dtos;

import com.familymoney.types.GroupId;
import com.familymoney.types.GroupInvitationToken;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

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
    UUID id, GroupId groupId, GroupInvitationToken token, Instant expiresAt) {}
