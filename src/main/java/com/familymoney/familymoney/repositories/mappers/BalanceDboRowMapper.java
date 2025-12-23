package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.BalanceDbo;
import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.springframework.jdbc.core.RowMapper;

public final class BalanceDboRowMapper implements RowMapper<BalanceDbo> {

  @Override
  public BalanceDbo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return BalanceDbo.builder()
        .id(BalanceId.fromString(rs.getString("id")))
        .groupId(GroupId.fromString(rs.getString("group_id")))
        .amount(
            Money.of(
                rs.getBigDecimal("amount"), Monetary.getCurrency(rs.getString("currency_code"))))
        .user1(UserId.fromString(rs.getString("user_id_1")))
        .user2(UserId.fromString(rs.getString("user_id_2")))
        .build();
  }
}
