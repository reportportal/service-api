package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecution;

public interface TmsTestCaseExecutionCommentService {

  TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);

  void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId);
}
