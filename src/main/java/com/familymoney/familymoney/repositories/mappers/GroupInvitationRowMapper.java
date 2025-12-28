package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.GroupInvitationDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupInvitationToken;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;

public final class GroupInvitationRowMapper implements RowMapper<GroupInvitationDbo> {

  @Override
  public GroupInvitationDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return GroupInvitationDbo.builder()
        .id(UUID.fromString(rs.getString("id")))
        .groupId(GroupId.fromString(rs.getString("group_id")))
        .token(GroupInvitationToken.fromString(rs.getString("token")))
        .expiresAt(rs.getTimestamp("expires_at").toInstant())
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .build();
  }
}
