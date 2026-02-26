package com.epam.reportportal.base.core.tms.validation;

import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validator implementation for {@link ValidTestFolderIdForBatchDuplicateTestCase} annotation.
 * <p>
 * This validator ensures that either testFolderId or testFolderName is provided in the request object, but not both.
 * </p>
 */

public class TestFolderIdForBatchDuplicateTestCaseValidator
    implements ConstraintValidator<ValidTestFolderIdForBatchDuplicateTestCase, BatchDuplicateTestCasesRQ> {

  @Override
  public void initialize(ValidTestFolderIdForBatchDuplicateTestCase constraintAnnotation) {
  }

  /**
   * Validates that at least one of testFolderId or testFolderName is not null.
   *
   * @param value   the object to validate
   * @param context the validation context
   * @return true if validation passes, false otherwise
   */
  @Override
  public boolean isValid(BatchDuplicateTestCasesRQ value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null objects if needed
    }

    var hasTestFolderId = Objects.nonNull(value.getTestFolderId());
    var hasTestFolderCreatedFromName =
        Objects.nonNull(value.getTestFolder()) && Objects.nonNull(value.getTestFolder().getName());

    if (hasTestFolderId && hasTestFolderCreatedFromName) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Either testFolderId or testFolderName must be provided and not empty")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
