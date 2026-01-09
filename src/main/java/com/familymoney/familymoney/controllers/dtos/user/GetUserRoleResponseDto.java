package com.familymoney.familymoney.controllers.dtos.user;

import com.familymoney.familymoney.types.Role;
import lombok.Builder;

@Builder
public record GetUserRoleResponseDto(Role role) {}
