package com.familymoney.familymoney.controllers.dtos.admin;

import java.util.List;
import lombok.Builder;

@Builder
public record GetUsersResponseDto(List<GetUserResponseDto> users) {}
