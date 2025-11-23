package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;

public interface TmsTestCaseExecutionCommentService {

  void update(TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);
}
