package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import com.familymoney.domains.users.types.UserId;
import java.util.Currency;

/**
 * DTO for creating a new group record in the database.
 *
 * @param id Unique identifier for the group.
 * @param name Name of the group. Cannot be empty.
 * @param description Optional textual description for the group. May be empty.
 * @param currency Default currency of the group.
 * @param createdBy UserId of the user who created the group.
 */
public record CreateGroupDto(
    GroupId id, GroupName name, Description description, Currency currency, UserId createdBy) {}
