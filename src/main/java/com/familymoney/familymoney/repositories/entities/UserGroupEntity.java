package com.familymoney.familymoney.repositories.entities;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UserGroupEntity(UserId userId, GroupId groupId, Instant joinedAt) {}
