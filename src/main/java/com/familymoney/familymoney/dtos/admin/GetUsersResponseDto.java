package com.familymoney.familymoney.dtos.admin;

import com.familymoney.familymoney.dtos.user.GetMyUserResponseDto;
import java.util.List;
import org.jspecify.annotations.NonNull;

public record GetUsersResponseDto(@NonNull List<GetMyUserResponseDto> users) {}
