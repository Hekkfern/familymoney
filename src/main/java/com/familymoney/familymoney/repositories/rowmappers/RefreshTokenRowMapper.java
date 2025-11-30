package com.familymoney.familymoney.repositories.rowmappers;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;

public final class RefreshTokenRowMapper implements RowMapper<RefreshTokenDbo> {

  @Override
  public RefreshTokenDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    Optional<Instant> usedAt =
        Optional.ofNullable(rs.getTimestamp("used_at")).map(Timestamp::toInstant);
    return RefreshTokenDbo.builder()
        .id(UUID.fromString(rs.getString("id")))
        .userId(new UserId(UUID.fromString(rs.getString("user_id"))))
        .token(new RefreshToken(rs.getString("token")))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .expiresAt(rs.getTimestamp("expires_at").toInstant())
        .isUsed(rs.getBoolean("is_used"))
        .usedAt(usedAt)
        .family(UUID.fromString(rs.getString("family")))
        .build();
  }
}
