package com.familymoney.domains.transactions.validations;

import com.familymoney.domains.transactions.controllers.dtos.CreateExpenseRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.UUID;

public class DifferentFromToValidator
    implements ConstraintValidator<DifferentFromTo, CreateExpenseRequestDto> {

  @Override
  public boolean isValid(CreateExpenseRequestDto dto, ConstraintValidatorContext context) {
    if (dto == null) return true; // other constraints handle null
    final UUID from = dto.from();
    final UUID to = dto.to();
    if (from == null || to == null) return true; // let @NotNull handle nulls

    if (!Objects.equals(from, to)) {
      return true;
    }

    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
        .addPropertyNode("to")
        .addConstraintViolation();
    return false;
  }
}
