package com.familymoney.domains.idempotency.repositories;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DefaultIdempotencyRepository implements IdempotencyRepository {

  private final DSLContext db;


}
