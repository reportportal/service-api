package com.epam.reportportal.base.core.tms.validation;

import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link ValidQualityStandardRQ}.
 */
public class QualityStandardRQValidator implements ConstraintValidator<ValidQualityStandardRQ, TmsQualityStandardRQ> {

  @Override
  public boolean isValid(TmsQualityStandardRQ value, ConstraintValidatorContext context) {
    if (value == null || value.getCriteria() == null) {
      return true; // let @NotEmpty/@NotNull handle that
    }
    int sum = value.getCriteria().stream()
        .mapToInt(c -> c.getMaxPoints() == null ? 0 : c.getMaxPoints())
        .sum();
    return sum == 100;
  }
}
