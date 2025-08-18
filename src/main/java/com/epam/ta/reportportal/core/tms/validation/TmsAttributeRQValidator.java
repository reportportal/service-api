package com.epam.ta.reportportal.core.tms.validation;

import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator implementation for {@link ValidTmsAttributeRQ} annotation.
 * <p>
 * This validator ensures that at either id or key of TmsAttributeRQ is not null
 * </p>
 */
public class TmsAttributeRQValidator
    implements ConstraintValidator<ValidTmsAttributeRQ, TmsAttributeRQ> {

  @Override
  public void initialize(ValidTmsAttributeRQ constraintAnnotation) {
    // No initialization needed
  }

  /**
   * Validates that either id or key of TmsAttributeRQ is not null.
   *
   * @param value   the object to validate
   * @param context the validation context
   * @return true if validation passes, false otherwise
   */
  @Override
  public boolean isValid(TmsAttributeRQ value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null objects if needed
    }

    var id = Objects.nonNull(value.getId());
    var hasKey = StringUtils.isNotBlank(value.getKey());

    if ((id && hasKey) || (!id && !hasKey)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Either tag id or tag key must be provided and not empty")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
