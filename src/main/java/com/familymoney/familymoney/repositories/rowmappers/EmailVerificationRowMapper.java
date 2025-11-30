package com.familymoney.familymoney.repositories.rowmappers;

import com.familymoney.familymoney.repositories.dbos.EmailVerificationDbo;
import com.familymoney.familymoney.types.EmailVerificationToken;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;

public final class EmailVerificationRowMapper implements RowMapper<EmailVerificationDbo> {

  @Override
  public EmailVerificationDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return EmailVerificationDbo.builder()
        .id(UUID.fromString(rs.getString("id")))
        .userId(new UserId(UUID.fromString(rs.getString("user_id"))))
        .token(new EmailVerificationToken(rs.getString("token")))
        .expiresAt(rs.getTimestamp("expires_at").toInstant())
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .build();
  }
}
