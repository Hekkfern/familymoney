package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.Description;
import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import javax.money.CurrencyUnit;

/**
 * DTO for creating a new group record in the database.
 *
 * @param id Unique identifier for the group.
 * @param name Name of the group. Cannot be empty.
 * @param description Optional textual description for the group. May be empty.
 * @param currency Default currency of the group.
 */
public record CreateGroupDto(
    GroupId id, GroupName name, Description description, CurrencyUnit currency) {}
