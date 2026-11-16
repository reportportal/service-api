package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;

public interface TmsTestCaseExecutionCommentService {

  /**
   * Creates or fully updates execution comment.
   */
  TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(Long projectId, TmsTestCaseExecution existingExecution,
      TmsTestCaseExecutionCommentRQ executionCommentRq);

  /**
   * Partially updates execution comment.
   */
  TmsTestCaseExecutionCommentRS patchTestCaseExecutionComment(Long projectId, TmsTestCaseExecution existingExecution,
      TmsTestCaseExecutionCommentRQ executionCommentRq);

  void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId);

  void deleteByLaunchId(Long launchId);

  void deleteTestCaseExecutionComment(long projectId, Long launchId, TmsTestCaseExecution execution);
}