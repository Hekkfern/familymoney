package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.validation.ValidGroupName;
import org.jspecify.annotations.Nullable;

public record UpdateGroupRequestDto(@Nullable @ValidGroupName String name) {}
