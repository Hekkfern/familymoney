package com.familymoney.familymoney.repositories.rowmappers;

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
    return new PasswordResetDbo(
        UUID.fromString(rs.getString("id")),
        new UserId(UUID.fromString(rs.getString("user_id"))),
        new PasswordResetToken(rs.getString("token")),
        rs.getTimestamp("expires_at").toInstant(),
        rs.getTimestamp("created_at").toInstant());
  }
}
