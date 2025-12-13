package com.familymoney.familymoney.controllers.dtos.group;

import jakarta.validation.constraints.NotBlank;

public record UpdateGroupRequestDto(@NotBlank String name) {}
