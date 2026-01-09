package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.UserName;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public final class UserRowMapper implements RowMapper<UserDbo> {

  @Override
  public UserDbo mapRow(ResultSet rs, final int rowNum) throws SQLException {
    return UserDbo.builder()
        .id(UserId.fromString(rs.getString("id")))
        .username(UserName.fromString(rs.getString("username")))
        .email(Email.fromString(rs.getString("email")))
        .hashedPassword(rs.getString("hashed_password"))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .updatedAt(rs.getTimestamp("updated_at").toInstant())
        .isEmailVerified(rs.getBoolean("is_email_verified"))
        .isEnabled(rs.getBoolean("is_enabled"))
        .build();
  }
}
