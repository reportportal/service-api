package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;

public interface TmsTestCaseExecutionCommentService {

  TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);

  TmsTestCaseExecutionCommentRS patchTestCaseExecutionComment(TmsTestCaseExecution existingExecution, TmsTestCaseExecutionCommentRQ executionCommentRQ);

  void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId);

  void deleteByLaunchId(Long launchId);

  void deleteTestCaseExecutionComment(long projectId, Long launchId, TmsTestCaseExecution execution);
}