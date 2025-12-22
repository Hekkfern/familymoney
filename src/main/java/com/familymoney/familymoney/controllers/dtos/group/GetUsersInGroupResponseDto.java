package com.familymoney.familymoney.controllers.dtos.group;

import com.familymoney.familymoney.types.UserId;
import java.util.List;

public record GetUsersInGroupResponseDto(List<UserId> userIds) {}
