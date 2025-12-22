package com.familymoney.familymoney.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRepository implements IGroupRepository{

    private final JdbcClient jdbcClient;
}
