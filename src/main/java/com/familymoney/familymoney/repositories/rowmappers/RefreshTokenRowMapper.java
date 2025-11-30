package com.familymoney.familymoney.repositories.rowmappers;

import com.familymoney.familymoney.repositories.dbos.RefreshTokenDbo;
import com.familymoney.familymoney.types.RefreshToken;
import com.familymoney.familymoney.types.UserId;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class RefreshTokenRowMapper implements RowMapper<RefreshTokenDbo> {

    @Override
    public RefreshTokenDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
        Optional<Instant> usedAt = Optional.ofNullable(rs.getTimestamp("used_at")).map(Timestamp::toInstant);
        return new RefreshTokenDbo(
                UUID.fromString(rs.getString("id")),
                new UserId(UUID.fromString(rs.getString("user_id"))),
                new RefreshToken(rs.getString("token")),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getBoolean("is_used"),
                usedAt,
                UUID.fromString(rs.getString("family"))
        );
    }
}
