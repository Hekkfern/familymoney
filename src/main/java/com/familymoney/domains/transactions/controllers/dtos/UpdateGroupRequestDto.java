package com.familymoney.domains.transactions.controllers.dtos;

import com.familymoney.domains.transactions.validations.ValidDescription;
import com.familymoney.domains.transactions.validations.ValidGroupName;
import org.jspecify.annotations.Nullable;

public record UpdateGroupRequestDto(
    @Nullable @ValidGroupName String name, @Nullable @ValidDescription String description) {}
