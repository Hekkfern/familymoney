package com.familymoney.domains.user.controllers.dtos;

import java.util.List;
import lombok.Builder;

@Builder
public record GetUsersResponseDto(List<GetUserResponseDto> users) {}
