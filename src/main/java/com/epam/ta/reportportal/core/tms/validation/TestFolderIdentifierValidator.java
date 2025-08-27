package com.epam.ta.reportportal.core.tms.validation;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.dto.NewTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Validator implementation for {@link ValidTestFolderIdentifier} annotation.
 * <p>
 * This validator ensures that either testFolderId or testFolderName is provided
 * in the request object, but not both.
 * </p>
 */
@Component
public class TestFolderIdentifierValidator
    implements ConstraintValidator<ValidTestFolderIdentifier, Object> {

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
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true; // Let @NotNull handle null objects if needed
    }

    Long testFolderId;
    NewTestFolderRQ newTestFolderRQ;

    if (value instanceof TmsTestCaseRQ tmsTestCaseRQ) {
      testFolderId = tmsTestCaseRQ.getTestFolderId();
      newTestFolderRQ = tmsTestCaseRQ.getTestFolder();
    } else if (value instanceof BatchDuplicateTestCasesRQ batchDuplicateTestCasesRQ) {
      testFolderId = batchDuplicateTestCasesRQ.getTestFolderId();
      newTestFolderRQ = batchDuplicateTestCasesRQ.getTestFolder();
    } else {
      throw new IllegalStateException("Wrong class annotated with @ValidTestFolderIdentifier: " + value.getClass().getName());
    }

    var hasTestFolderId = Objects.nonNull(testFolderId);
    var hasTestFolderCreatedFromName = Objects.nonNull(newTestFolderRQ);

    if (hasTestFolderId && hasTestFolderCreatedFromName) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
              "Either testFolderId or testFolderName must be provided and not empty")
          .addConstraintViolation();
      return false;
    }

    return true;
  }

  public void validate(Long testFolderId, String testFolderName) {
    var hasTestFolderId = Objects.nonNull(testFolderId);
    var hasTestFolderName = StringUtils.isNotBlank(testFolderName);

    if ((hasTestFolderId && hasTestFolderName) || (!hasTestFolderId && !hasTestFolderName)) {
      throw new ReportPortalException(
          ErrorType.BAD_REQUEST_ERROR,
          "Either testFolderId or testFolderName must be provided and not empty"
      );
    }
  }
}
