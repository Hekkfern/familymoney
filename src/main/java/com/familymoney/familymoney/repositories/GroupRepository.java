package com.familymoney.familymoney.repositories;

import com.familymoney.familymoney.repositories.dbos.GroupDbo;
import com.familymoney.familymoney.repositories.dbos.UpdateGroupDbo;
import com.familymoney.familymoney.repositories.dbos.UserGroupDbo;
import com.familymoney.familymoney.repositories.mappers.GroupRowMapper;
import com.familymoney.familymoney.repositories.mappers.UserGroupRowMapper;
import com.familymoney.familymoney.types.GroupId;
import com.familymoney.familymoney.types.GroupName;
import com.familymoney.familymoney.types.UserId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
      final GroupName name,
      final String description,
      final CurrencyUnit currency,
      final UserId owner) {
    val sql =
        """
        INSERT INTO groups (name, description, currency_code, created_by)
        VALUES (:name, :description, :currency, :owner)
        RETURNING id, name, description, currency_code, created_by, created_at, updated_at
        """;
    return jdbcClient
        .sql(sql)
        .param("name", name.value())
        .param("description", description)
        .param("currency", currency.getCurrencyCode())
        .param("owner", owner.value())
        .query(new GroupRowMapper())
        .optional();
  }

  @Override
  public boolean updateById(final GroupId id, final UpdateGroupDbo data) {
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
            .param("id", id.value())
            .param("name", data.getName() != null ? data.getName().value() : null)
            .param("description", data.getDescription() != null ? data.getDescription() : null)
            .update();
    return rowsAffected > 0;
  }

  @Override
  public boolean deleteById(final GroupId id) {
    val sql =
        """
        DELETE FROM groups
        WHERE id = :id
        """;
    val rowsAffected = jdbcClient.sql(sql).param("id", id.value()).update();
    return rowsAffected > 0;
  }

  @Override
  public Page<GroupDbo> findByUserId(final UserId userId, final Pageable pageable) {
    val rowCountSql =
        """
        SELECT COUNT(1)
        FROM user_groups
        WHERE user_id = :userId
        """;
    val total =
        jdbcClient.sql(rowCountSql).param("userId", userId.value()).query(Long.class).single();
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
            .param("userId", userId.value())
            .param("limit", pageable.getPageSize())
            .param("offset", pageable.getOffset())
            .query(new GroupRowMapper())
            .list();
    return new PageImpl<>(data, pageable, total);
  }

  @Override
  public Optional<GroupDbo> findById(final GroupId id) {
    val sql =
        """
        SELECT id, name, description, currency_code, created_by, created_at, updated_at
        FROM groups
        WHERE id = :id
        """;
    return jdbcClient.sql(sql).param("id", id.value()).query(new GroupRowMapper()).optional();
  }

  @Override
  public List<UserId> findUserIdsByGroupId(final GroupId id) {
    val sql =
        """
        SELECT user_id
        FROM user_groups
        WHERE group_id = :groupId
        """;
    return jdbcClient.sql(sql).param("groupId", id.value()).query(String.class).stream()
        .filter(Objects::nonNull)
        .map(UserId::fromString)
        .toList();
  }

  @Override
  public boolean isUserInGroup(final UserId userId, final GroupId groupId) {
    val sql =
        """
        SELECT COUNT(1)
        FROM user_groups
        WHERE user_id = :userId
          AND group_id = :groupId
        """;
    val count =
        jdbcClient
            .sql(sql)
            .param("userId", userId.value())
            .param("groupId", groupId.value())
            .query(Long.class)
            .single();
    return count > 0;
  }

  @Override
  public Optional<UserGroupDbo> addUser(UserId userId, GroupId groupId) {
    val sql =
        """
        INSERT INTO user_groups (user_id, group_id)
        VALUES (:userId, :groupId)
        RETURNING user_id, group_id, joined_at
        """;
    return jdbcClient
        .sql(sql)
        .param("userId", userId.value())
        .param("groupId", groupId.value())
        .query(new UserGroupRowMapper())
        .optional();
  }

  @Override
  public boolean deleteUser(UserId userId, GroupId groupId) {
    // TODO
    return false;
  }
}
