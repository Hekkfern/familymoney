package com.familymoney.repositories.dtos;

import com.familymoney.types.GroupId;
import com.familymoney.types.GroupName;
import lombok.Builder;

import javax.money.CurrencyUnit;

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
