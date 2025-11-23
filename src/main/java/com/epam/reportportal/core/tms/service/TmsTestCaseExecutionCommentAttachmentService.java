package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;

/**
 * Service interface for managing TMS test case execution comment attachments.
 */
public interface TmsTestCaseExecutionCommentAttachmentService {

  /**
   * Creates attachments for execution comment.
   */
  void createAttachments(TmsTestCaseExecutionComment comment,
      TmsTestCaseExecutionCommentRQ commentRQ);

  /**
   * Updates attachments for execution comment (replaces all existing).
   */
  void updateAttachments(TmsTestCaseExecutionComment existingComment,
      TmsTestCaseExecutionCommentRQ commentRQ);

  /**
   * Deletes all attachment relationships by execution ID.
   */
  void deleteAllByExecutionId(Long executionId);

  void deleteByLaunchId(Long launchId);
}
