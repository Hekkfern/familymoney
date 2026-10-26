package com.familymoney.domains.transactions.events;

import com.familymoney.domains.transactions.types.GroupInvitationToken;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.Email;
import com.familymoney.domains.users.types.UserId;
import com.familymoney.domains.users.types.UserName;

/**
 * Requests a group invitation email after the invitation transaction commits.
 *
 * @param userId identifier of the invited user
 * @param email recipient email address
 * @param username recipient username
 * @param groupName name of the group
 * @param invitationToken raw single-use invitation token
 */
public record GroupInvitationRequestedEvent(
    UserId userId,
    Email email,
    UserName username,
    GroupName groupName,
    GroupInvitationToken invitationToken) {}
