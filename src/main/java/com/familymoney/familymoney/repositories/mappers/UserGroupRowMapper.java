package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.UserGroupDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public final class UserGroupRowMapper implements RowMapper<UserGroupDbo> {

  @Override
  public UserGroupDbo mapRow(ResultSet rs, final int rowNum) throws SQLException {
    return UserGroupDbo.builder()
        .userId(UserId.fromString(rs.getString("user_id")))
        .groupId(GroupId.fromString(rs.getString("group_id")))
        .joinedAt(rs.getTimestamp("joined_at").toInstant())
        .build();
  }
}
