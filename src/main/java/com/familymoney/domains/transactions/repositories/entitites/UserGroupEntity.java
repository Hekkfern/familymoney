package com.familymoney.domains.transactions.repositories.entitites;

import com.familymoney.domains.transactions.types.GroupId;
import com.familymoney.domains.user.types.UserId;
import java.time.Instant;

public record UserGroupEntity(UserId userId, GroupId groupId, Instant joinedAt) {}
