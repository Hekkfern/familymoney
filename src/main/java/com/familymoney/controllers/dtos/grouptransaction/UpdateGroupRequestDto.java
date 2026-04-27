package com.familymoney.controllers.dtos.grouptransaction;

import com.familymoney.validation.ValidGroupName;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateGroupRequestDto(
    @Nullable @ValidGroupName String name,
    @Nullable @Size(min = 1, max = 255) String description) {}
