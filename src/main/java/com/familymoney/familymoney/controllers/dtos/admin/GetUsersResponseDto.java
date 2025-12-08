package com.familymoney.familymoney.controllers.dtos.admin;

import java.util.List;
import org.jspecify.annotations.NonNull;

public record GetUsersResponseDto(@NonNull List<GetUserResponseDto> users) {}
