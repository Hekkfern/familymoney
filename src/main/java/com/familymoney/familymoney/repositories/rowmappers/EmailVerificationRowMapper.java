package com.familymoney.familymoney.repositories.rowmappers;

import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class EmailVerificationRowMapper implements RowMapper<EmailVerificationDbo> {

  @Override
  public EmailVerificationDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new EmailVerificationDbo(
        UUID.fromString(rs.getString("id")),
        new UserId(UUID.fromString(rs.getString("user_id"))),
        new EmailVerificationToken(rs.getString("token")),
        rs.getTimestamp("expires_at").toInstant(),
        rs.getTimestamp("created_at").toInstant());
  }
}
