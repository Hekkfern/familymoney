package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.TransactionDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateTransactionDbo;
import com.familymoney.familymoney.repositories.mappers.TransactionRowMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.TransactionId;
import com.familymoney.familymoney.types.UserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.javamoney.moneta.Money;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepository implements ITransactionRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<TransactionDbo> create(
      String description, GroupId groupId, Money amount, UserId lender, UserId borrower) {
    val sql =
        """
        INSERT INTO transactions (description, group_id, amount, currency_code, lender, borrower)
        VALUES (:description, :groupId, :amount, :currencyCode, :lender, :borrower)
        RETURNING id, description, group_id, currency_code, lender,borrower, created_at, updated_at
        """;
    return jdbcClient
        .sql(sql)
        .param("description", description)
        .param("groupId", groupId.value())
        .param("amount", amount.getNumber().toString())
        .param("currencyCode", amount.getCurrency().getCurrencyCode())
        .param("lender", lender.value())
        .param("borrower", borrower.value())
        .query(new TransactionRowMapper())
        .optional();
  }

  @Override
  public boolean updateById(TransactionId id, UpdateTransactionDbo data) {
    val sql =
        """
        UPDATE transactions
        SET amount = COALESCE(:amount, amount),
            currency_code = COALESCE(:currencyCode, currency_code),
            description = COALESCE(:description, description),
            lender = COALESCE(:lender, lender),
            borrower = COALESCE(:borrower, borrower)
        WHERE id = :id
        """;
    val rowsAffected =
        jdbcClient
            .sql(sql)
            .param("id", id.value())
            .param(
                "amount", data.getAmount() != null ? data.getAmount().getNumber().toString() : null)
            .param(
                "currencyCode",
                data.getAmount() != null ? data.getAmount().getCurrency().getCurrencyCode() : null)
            .param("description", data.getDescription() != null ? data.getDescription() : null)
            .param("lender", data.getLender() != null ? data.getLender().value() : null)
            .param("borrower", data.getBorrower() != null ? data.getBorrower().value() : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(TransactionId id) {
    val sql =
        """
        DELETE FROM transactions
        WHERE id = :id
        """;
    val rowsAffected = jdbcClient.sql(sql).param("id", id.value()).update();
    return rowsAffected > 0;
  }

  @Override
  public Optional<TransactionDbo> findById(TransactionId id) {
    val sql =
        """
        SELECT id, description, group_id, currency_code, lender,borrower, created_at, updated_at
        FROM transactions
        WHERE id = :id
        """;
    return jdbcClient
        .sql(sql)
        .param("id", id.value())
        .query(new TransactionRowMapper())
        .optional();
  }

  @Override
  public Page<TransactionDbo> findAllByGroupId(GroupId groupId, Pageable pageable) {
    val rowCountSql =
        """
        SELECT COUNT(1)
        FROM transactions
        where group_id = :groupId
        """;
    val total =
        jdbcClient.sql(rowCountSql).param("groupId", groupId.value()).query(Long.class).single();
    val querySql =
        """
        SELECT id, description, group_id, currency_code, lender,borrower, created_at, updated_at
        FROM transactions
        LIMIT :limit
        OFFSET :offset
        """;
    val data =
        jdbcClient
            .sql(querySql)
            .param("limit", pageable.getPageSize())
            .param("offset", pageable.getOffset())
            .query(new TransactionRowMapper())
            .list();
    return new PageImpl<>(data, pageable, total);
  }
}
