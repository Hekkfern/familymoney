package com.familymoney.familymoney.validation;

import com.familymoney.familymoney.controllers.dtos.grouptransaction.CreateTransactionRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import lombok.val;

public class DifferentFromToValidator
    implements ConstraintValidator<DifferentFromTo, CreateTransactionRequestDto> {

  @Override
  public boolean isValid(CreateTransactionRequestDto dto, ConstraintValidatorContext context) {
    if (dto == null) return true; // other constraints handle null
    val from = dto.from();
    val to = dto.to();
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
