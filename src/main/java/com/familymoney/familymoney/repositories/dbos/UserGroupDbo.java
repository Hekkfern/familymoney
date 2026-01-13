package com.familymoney.familymoney.repositories.dbos;

import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UserGroupDbo(UserId userId, GroupId groupId, Instant joinedAt) {}
