package com.familymoney.domains.transactions.types;

import com.familymoney.testutils.RandomUtil;

public record GroupInvitationToken(String value) {

  private static final int LENGTH = 64;

  public static GroupInvitationToken fromString(String value) {
    return new GroupInvitationToken(value);
  }

  public static GroupInvitationToken generate() {
    return new GroupInvitationToken(RandomUtil.generateRandomString(LENGTH));
  }

  @Override
  public String toString() {
    return value;
  }
}
