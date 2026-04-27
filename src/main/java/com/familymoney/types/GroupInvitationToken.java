package com.familymoney.types;

import com.familymoney.utils.RandomUtil;

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
