package com.familymoney.domains.transactions.repositories.dtos;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.transactions.types.GroupName;
import javax.money.CurrencyUnit;
import lombok.Builder;

/**
 * DTO for creating a new group record in the database.
 *
 * @param id Unique identifier for the group.
 * @param name Name of the group. Cannot be empty.
 * @param description Optional textual description for the group. May be empty.
 * @param currency Default currency of the group.
 */
@Builder
public record CreateGroupDto(
    GroupId id, GroupName name, String description, CurrencyUnit currency) {
  public CreateGroupDto {
    assert !name.value().isEmpty() : "Group name cannot be empty";
  }
}
