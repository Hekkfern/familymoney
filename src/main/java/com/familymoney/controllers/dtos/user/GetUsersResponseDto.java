package com.familymoney.controllers.dtos.user;

import java.util.List;
import lombok.Builder;

@Builder
public record GetUsersResponseDto(List<GetUserResponseDto> users) {}
