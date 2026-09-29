package com.familymoney.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.familymoney.generated.Keys;
import java.sql.SQLException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class ConstraintViolationUtilsTest {

  private static final String FK_VIOLATION_MESSAGE_TEMPLATE =
      "ERROR: insert or update on table \"user_groups\" violates foreign key constraint \"%s\"";

  @Nested
  class IsConstraintViolated {

    @Test
    void returns_true_when_root_cause_references_constraint() {
      // given
      final DataIntegrityViolationException exception =
          exceptionWithRootCauseMessage(
              FK_VIOLATION_MESSAGE_TEMPLATE.formatted(
                  Keys.USER_GROUPS__USER_GROUPS_USER_ID_FKEY.getName()));

      // when
      final boolean result =
          ConstraintViolationUtils.isConstraintViolated(
              exception, Keys.USER_GROUPS__USER_GROUPS_USER_ID_FKEY);

      // then
      assertThat(result).isTrue();
    }

    @Test
    void returns_false_when_root_cause_references_another_constraint() {
      // given
      final DataIntegrityViolationException exception =
          exceptionWithRootCauseMessage(
              FK_VIOLATION_MESSAGE_TEMPLATE.formatted(
                  Keys.USER_GROUPS__USER_GROUPS_GROUP_ID_FKEY.getName()));

      // when
      final boolean result =
          ConstraintViolationUtils.isConstraintViolated(
              exception, Keys.USER_GROUPS__USER_GROUPS_USER_ID_FKEY);

      // then
      assertThat(result).isFalse();
    }

    @Test
    void returns_false_when_root_cause_has_no_message() {
      // given
      final DataIntegrityViolationException exception =
          new DataIntegrityViolationException("integrity violation", new SQLException());

      // when
      final boolean result =
          ConstraintViolationUtils.isConstraintViolated(
              exception, Keys.USER_GROUPS__USER_GROUPS_USER_ID_FKEY);

      // then
      assertThat(result).isFalse();
    }

    private static DataIntegrityViolationException exceptionWithRootCauseMessage(
        final String message) {
      return new DataIntegrityViolationException("integrity violation", new SQLException(message));
    }
  }
}
