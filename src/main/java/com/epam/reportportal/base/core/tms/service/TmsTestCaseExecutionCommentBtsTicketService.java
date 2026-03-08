package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;

/**
 * Service interface for managing TMS test case execution comment BTS tickets.
 */
public interface TmsTestCaseExecutionCommentBtsTicketService {

  /**
   * Creates BTS tickets for execution comment.
   */
  void createBtsTickets(TmsTestCaseExecutionComment comment, TmsTestCaseExecutionCommentRQ commentRQ);

  /**
   * Updates BTS tickets for execution comment (replaces all existing).
   */
  void updateBtsTickets(TmsTestCaseExecutionComment existingComment, TmsTestCaseExecutionCommentRQ commentRQ);

  /**
   * Deletes all BTS ticket relationships by execution ID.
   */
  void deleteAllByExecutionId(Long executionId);

  /**
   * Deletes all BTS ticket relationships by launch ID.
   */
  void deleteByLaunchId(Long launchId);
}
