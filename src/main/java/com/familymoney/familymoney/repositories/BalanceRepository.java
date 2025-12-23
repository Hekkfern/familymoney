package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.BalanceDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateBalanceDbo;
import com.familymoney.familymoney.repositories.mappers.BalanceDboRowMapper;
import com.familymoney.familymoney.types.BalanceId;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.javamoney.moneta.Money;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BalanceRepository implements IBalanceRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<BalanceDbo> create(GroupId groupId, Money amount, UserId user1, UserId user2) {
    val sql =
        """
        INSERT INTO balances (group_id, amount, currency_code, user_id_1, user_id_2)
        VALUES (:groupId, :amount, :currencyCode, :user1, :user2)
        RETURNING id, group_id, amount, currency_code, user_id_1, user_id_2
        """;
    return jdbcClient
        .sql(sql)
        .param("groupId", groupId.value())
        .param("amount", amount.getNumber().toString())
        .param("currencyCode", amount.getCurrency().getCurrencyCode())
        .param("user1", user1.value())
        .param("user2", user2.value())
        .query(new BalanceDboRowMapper())
        .optional();
  }

  @Override
  public List<BalanceDbo> findByUserAndGroup(UserId userId, GroupId groupId) {
    val sql =
        """
        SELECT id, group_id, amount, currency_code, user_id_1, user_id_2
        FROM balances
        WHERE (user_id_1 = :userId OR user_id_2 = :userId) AND group_id = :groupId
        """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.value())
        .param("groupId", groupId.value())
        .query(new BalanceDboRowMapper())
        .list();
  }

  @Override
  public boolean updateById(BalanceId id, UpdateBalanceDbo data) {
    val sql =
        """
        UPDATE balances
        SET amount = COALESCE(:amount, amount),
            currency_code = COALESCE(:currencyCode, currency_code),
            user_id_1 = COALESCE(:user1, user_id_1),
            user_id_2 = COALESCE(:user2, user_id_2)
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
            .param("user1", data.getUser1() != null ? data.getUser1().value() : null)
            .param("user2", data.getUser2() != null ? data.getUser2().value() : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public Optional<BalanceDbo> findById(BalanceId id) {
    val sql =
        """
        SELECT id, group_id, amount, currency_code, user_id_1, user_id_2
        FROM balances
        WHERE id = :id
        """;
    return jdbcClient.sql(sql).param("id", id.value()).query(new BalanceDboRowMapper()).optional();
  }
}
