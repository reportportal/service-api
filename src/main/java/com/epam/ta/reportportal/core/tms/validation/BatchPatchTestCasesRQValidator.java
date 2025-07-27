package com.epam.ta.reportportal.core.tms.validation;

import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator implementation for {@link ValidBatchPatchTestCasesRQ} annotation.
 * <p>
 * This validator ensures that at least one field is provided
 * in the BatchPatchTestCasesRQ object, but not necessarily all.
 * </p>
 */
public class BatchPatchTestCasesRQValidator
    implements ConstraintValidator<ValidBatchPatchTestCasesRQ, BatchPatchTestCasesRQ> {

  @Override
  public void initialize(ValidBatchPatchTestCasesRQ constraintAnnotation) {
    // No initialization needed
  }

  /**
   * Validates that at least one field of BatchPatchTestCasesRQ is not null.
   *
   * @param value   the object to validate
   * @param context the validation context
   * @return true if validation passes, false otherwise
   */
  @Override
  public boolean isValid(BatchPatchTestCasesRQ value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null objects if needed
    }

    var hasTestFolderId = Objects.nonNull(value.getTestFolderId());
    var hasPriority = StringUtils.isNotBlank(value.getPriority());
    var hasTags = Objects.nonNull(value.getTags());

    if (!hasTestFolderId && !hasPriority && !hasTags) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Either folderId or priroty or tags must be provided and not empty")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
