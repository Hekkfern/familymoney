package com.familymoney.domains.transactions.repositories.dbos;

import com.familymoney.domains.transactions.types.ExpirationTime;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.users.types.UserId;
import java.time.Instant;

/**
 * DTO for creating a new group invitation record in the database
 *
 * @param groupId the identifier of the group for which the invitation is being created
 * @param userId the identifier of the user being invited to the group
 * @param token the token associated with the group invitation
 * @param createdBy the identifier of the user who created the group invitation
 * @param expiresAt the timestamp indicating when the invitation expires, after which it should no
 *     longer be valid
 * @param lastSentAt timestamp when the group invitation was last requested
 */
public record CreateGroupInvitationDbo(
    GroupId groupId,
    UserId userId,
    GroupInvitationToken token,
    UserId createdBy,
    ExpirationTime expiresAt,
    Instant lastSentAt) {}
