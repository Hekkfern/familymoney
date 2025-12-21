package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;

public final class UserDboRowMapper implements RowMapper<UserDbo> {

  @Override
  public UserDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return UserDbo.builder()
        .id(new UserId(UUID.fromString(rs.getString("id"))))
        .username(new Username(rs.getString("username")))
        .email(new Email(rs.getString("email")))
        .hashedPassword(rs.getString("hashed_password"))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .updatedAt(rs.getTimestamp("updated_at").toInstant())
        .isEmailVerified(rs.getBoolean("email_verified"))
        .isEnabled(rs.getBoolean("is_enabled"))
        .build();
  }
}
