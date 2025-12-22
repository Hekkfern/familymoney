package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.money.Monetary;
import org.springframework.jdbc.core.RowMapper;

public final class GroupDboRowMapper implements RowMapper<GroupDbo> {

  @Override
  public GroupDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return GroupDbo.builder()
        .id(GroupId.fromString(rs.getString("id")))
        .name(GroupName.fromString(rs.getString("name")))
        .description(rs.getString("description"))
        .currency(Monetary.getCurrency(rs.getString("currency_code")))
        .createdBy(UserId.fromString(rs.getString("created_by")))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .updatedAt(rs.getTimestamp("updated_at").toInstant())
        .build();
  }
}
