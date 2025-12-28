package com.familymoney.familymoney.controllers.dtos.admin;

import lombok.Builder;

import java.util.List;

@Builder
public record GetUsersResponseDto(List<GetUserResponseDto> users) {}
