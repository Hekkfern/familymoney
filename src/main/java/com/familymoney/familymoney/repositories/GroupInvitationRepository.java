package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.GroupInvitationDbo;
import com.familymoney.familymoney.repositories.mappers.GroupInvitationRowMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupInvitationRepository implements IGroupInvitationRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<GroupInvitationDbo> create(
      final GroupId groupId, final GroupInvitationToken token, final Instant expiresAt) {
    val sql =
        """
        INSERT INTO group_invitations (group_id, token, expires_at)
        VALUES (:groupId, :token, :expiresAt)
        RETURNING id, group_id, token, created_at, expires_at
        """;
    return jdbcClient
        .sql(sql)
        .param("groupId", groupId.value())
        .param("token", token.value())
        .param("expiresAt", OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
        .query(new GroupInvitationRowMapper())
        .optional();
  }

  @Override
  public Optional<GroupInvitationDbo> findByToken(final GroupInvitationToken token) {
    val sql =
        """
        SELECT id, group_id, token, created_at, expires_at
        FROM group_invitations
        WHERE token = :token
        """;
    return jdbcClient
        .sql(sql)
        .param("token", token.value())
        .query(new GroupInvitationRowMapper())
        .optional();
  }

  @Override
  public void deleteOlderThan(final Duration cutoff) {
    val sql =
        """
        DELETE FROM group_invitations
        WHERE created_at < NOW() - INTERVAL ':duration seconds'
        """;
    jdbcClient.sql(sql).param("duration", cutoff.toSeconds()).update();
  }
}
