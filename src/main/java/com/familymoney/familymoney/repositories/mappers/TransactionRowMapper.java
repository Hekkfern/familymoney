package com.familymoney.familymoney.repositories.mappers;

import com.familymoney.familymoney.repositories.dbos.TransactionDbo;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.money.Monetary;
import org.javamoney.moneta.Money;
import org.springframework.jdbc.core.RowMapper;

public final class TransactionRowMapper implements RowMapper<TransactionDbo> {

  @Override
  public TransactionDbo mapRow(ResultSet rs, final int rowNum) throws SQLException {
    return TransactionDbo.builder()
        .id(TransactionId.fromString(rs.getString("id")))
        .description(rs.getString("username"))
        .groupId(GroupId.fromString(rs.getString("email")))
        .amount(
            Money.of(
                rs.getBigDecimal("amount"), Monetary.getCurrency(rs.getString("currency_code"))))
        .lender(UserId.fromString(rs.getString("from")))
        .borrower(UserId.fromString(rs.getString("to")))
        .doneAt(rs.getTimestamp("done_at").toInstant())
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .updatedAt(rs.getTimestamp("updated_at").toInstant())
        .build();
  }
}
