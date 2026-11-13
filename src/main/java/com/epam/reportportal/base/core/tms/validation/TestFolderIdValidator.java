package com.epam.reportportal.base.core.tms.validation;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TestFolderIdValidator {

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
