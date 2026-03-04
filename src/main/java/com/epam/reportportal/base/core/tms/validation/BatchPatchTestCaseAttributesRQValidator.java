package com.epam.reportportal.base.core.tms.validation;

import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Validator implementation for {@link ValidBatchPatchTestCaseAttributesRQ} annotation.
 * <p>
 * This validator ensures that at least one field is provided in the BatchPatchTestCaseAttributesRQ object, but not
 * necessarily all.
 * </p>
 */
public class BatchPatchTestCaseAttributesRQValidator
    implements ConstraintValidator<ValidBatchPatchTestCaseAttributesRQ, BatchPatchTestCaseAttributesRQ> {

  @Override
  public void initialize(ValidBatchPatchTestCaseAttributesRQ constraintAnnotation) {
    // No initialization needed
  }

  /**
   * Validates that at least one field of BatchPatchTestCaseAttributesRQ is not null.
   *
   * @param value   the object to validate
   * @param context the validation context
   * @return true if validation passes, false otherwise
   */
  @Override
  public boolean isValid(BatchPatchTestCaseAttributesRQ value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null objects if needed
    }

    var hasAttributeIdsToAdd = CollectionUtils.isNotEmpty(value.getAttributeIdsToAdd());
    var hasAttributeIdsToRemove = CollectionUtils.isNotEmpty(value.getAttributesToRemove());

    if (!hasAttributeIdsToAdd && !hasAttributeIdsToRemove) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Either attributes to add or to remove must be provided and not empty")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
