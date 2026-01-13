package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.GroupInvitationDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public interface IGroupInvitationRepository {

  Optional<GroupInvitationDbo> create(
      GroupId groupId, GroupInvitationToken token, Instant expiresAt);

  Optional<GroupInvitationDbo> findByToken(GroupInvitationToken token);

  boolean deleteByToken(GroupInvitationToken token);

  void deleteOlderThan(Duration cutoff);
}
