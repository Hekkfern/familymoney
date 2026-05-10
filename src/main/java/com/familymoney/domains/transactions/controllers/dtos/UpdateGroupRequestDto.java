package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.ValidGroupName;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateGroupRequestDto(
    @Nullable @ValidGroupName String name,
    @Nullable @Size(min = 1, max = 255) String description) {}
