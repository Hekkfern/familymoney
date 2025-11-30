package com.familymoney.familymoney.repositories.rowmappers;

import com.familymoney.familymoney.repositories.dbos.UserDbo;
import com.familymoney.familymoney.types.Email;
import com.familymoney.familymoney.types.UserId;
import com.familymoney.familymoney.types.Username;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class UserDboRowMapper implements RowMapper<UserDbo> {

    @Override
    public UserDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserDbo(
                new UserId(UUID.fromString(rs.getString("id"))),
                new Username(rs.getString("username")),
                new Email(rs.getString("email")),
                rs.getString("hashed_password"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getBoolean("email_verified"),
                rs.getBoolean("is_enabled")
        );
    }
}
