package com.familymoney.controllers.dtos.user;

import com.familymoney.types.Role;
import lombok.Builder;

@Builder
public record GetUserRoleResponseDto(Role role) {}
