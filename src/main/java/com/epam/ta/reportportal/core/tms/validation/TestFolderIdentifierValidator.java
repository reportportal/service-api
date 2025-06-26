package com.epam.ta.reportportal.core.tms.validation;

import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.validation.ValidTestFolderIdentifier;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator implementation for {@link ValidTestFolderIdentifier} annotation.
 * <p>
 * This validator ensures that either testFolderId or testFolderName is provided
 * in the TmsTestCaseTestFolderRQ object, but not necessarily both.
 * </p>
 */
public class TestFolderIdentifierValidator
    implements ConstraintValidator<ValidTestFolderIdentifier, TmsTestCaseTestFolderRQ> {

  @Override
  public void initialize(ValidTestFolderIdentifier constraintAnnotation) {
    // No initialization needed
  }

  /**
   * Validates that at least one of testFolderId or testFolderName is not null.
   *
   * @param value   the object to validate
   * @param context the validation context
   * @return true if validation passes, false otherwise
   */
  @Override
  public boolean isValid(TmsTestCaseTestFolderRQ value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null objects if needed
    }

    var hasTestFolderId = Objects.nonNull(value.getTestFolderId());
    var hasTestFolderName = StringUtils.isNotBlank(value.getTestFolderName());

    if ((hasTestFolderId && hasTestFolderName) || (!hasTestFolderId && !hasTestFolderName)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Either testFolderId or testFolderName must be provided and not empty")
          .addConstraintViolation();
      return false;
    }

    return true;
  }
}
