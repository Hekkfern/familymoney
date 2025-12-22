package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.PasswordResetDbo;
import com.familymoney.familymoney.types.PasswordResetToken;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;

public final class PasswordResetRowMapper implements RowMapper<PasswordResetDbo> {

  @Override
  public PasswordResetDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return PasswordResetDbo.builder()
        .id(UUID.fromString(rs.getString("id")))
        .userId(UserId.fromString(rs.getString("user_id")))
        .token(PasswordResetToken.fromString(rs.getString("token")))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .expiresAt(rs.getTimestamp("expires_at").toInstant())
        .build();
  }
}
