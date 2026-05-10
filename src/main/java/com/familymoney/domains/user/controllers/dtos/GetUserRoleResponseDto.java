package com.familymoney.domains.user.controllers.dtos;

import com.familymoney.domains.user.types.Role;
import lombok.Builder;

@Builder
public record GetUserRoleResponseDto(Role role) {}
