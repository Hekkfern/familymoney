package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateGroupDbo;
import com.familymoney.familymoney.repositories.mappers.GroupDboRowMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.util.Optional;
import java.util.UUID;
import javax.money.CurrencyUnit;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRepository implements IGroupRepository {

  private final JdbcClient jdbcClient;

  @Override
  public Optional<GroupDbo> create(
      GroupName name, String description, CurrencyUnit currency, UserId owner) {
    val sql =
        """
        INSERT INTO groups (name, description, currency_code, created_by)
        VALUES (:name, :description, :currency, :owner)
        RETURNING id, name, description, currency_code, created_by, created_at, updated_at
        """;
    return jdbcClient
        .sql(sql)
        .param("name", name.toString())
        .param("description", description)
        .param("currency", currency.getCurrencyCode())
        .param("owner", owner.toString())
        .query(new GroupDboRowMapper())
        .optional();
  }

  @Override
  public boolean updateById(GroupId id, UpdateGroupDbo data) {
    val sql =
        """
        UPDATE groups
        SET name = COALESCE(:name, name),
            description = COALESCE(:description, description)
        WHERE id = :id
        """;
    val rowsAffected =
        jdbcClient
            .sql(sql)
            .param("id", id.toString())
            .param("name", data.getName() != null ? data.getName().toString() : null)
            .param("description", data.getDescription() != null ? data.getDescription() : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(GroupId id) {
    val sql =
        """
        DELETE FROM groups
        WHERE id = :id
        """;
    val rowsAffected = jdbcClient.sql(sql).param("id", id.toString()).update();
    return rowsAffected > 0;
  }

  @Override
  public Page<GroupDbo> findAllByUserId(UserId userId, Pageable pageable) {
    val rowCountSql =
        """
        SELECT COUNT(1)
        FROM user_groups
        WHERE user_id = :userId
        """;
    val total =
        jdbcClient.sql(rowCountSql).param("userId", userId.toString()).query(Long.class).single();
    val querySql =
        """
        SELECT g.id, g.name, g.description, g.currency_code, g.created_by, g.created_at, g.updated_at
        FROM user_groups ug
        INNER JOIN groups g ON g.id = ug.group_id
        WHERE ug.user_id = :userId
        LIMIT :limit
        OFFSET :offset
        """;
    val data =
        jdbcClient
            .sql(querySql)
            .param("userId", userId.toString())
            .param("limit", pageable.getPageSize())
            .param("offset", pageable.getOffset())
            .query(new GroupDboRowMapper())
            .list();
    return new PageImpl<>(data, pageable, total);
  }

  @Override
  public Optional<GroupDbo> findById(GroupId id) {
    val sql =
        """
        SELECT id, name, description, currency_code, created_by, created_at, updated_at
        FROM groups
        WHERE id = :id
        """;
    return jdbcClient.sql(sql).param("id", id.toString()).query(new GroupDboRowMapper()).optional();
  }
}
