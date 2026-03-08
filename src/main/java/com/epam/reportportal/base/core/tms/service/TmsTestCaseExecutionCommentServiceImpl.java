package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRS;
import com.epam.reportportal.base.core.tms.mapper.TmsTestCaseExecutionCommentMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.HashSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsTestCaseExecutionCommentServiceImpl implements
    TmsTestCaseExecutionCommentService {

  private static final String TEST_CASE_EXECUTION_COMMENT_IN_EXECUTION =
      "Comment for Test Case execution: %d for Launch: %d";

  private final TmsTestCaseExecutionCommentRepository tmsTestCaseExecutionCommentRepository;
  private final TmsTestCaseExecutionCommentAttachmentService tmsTestCaseExecutionCommentAttachmentService;
  private final TmsTestCaseExecutionCommentBtsTicketService tmsTestCaseExecutionCommentBtsTicketService;
  private final TmsTestCaseExecutionCommentMapper tmsTestCaseExecutionCommentMapper;

  @Override
  @Transactional
  public TmsTestCaseExecutionCommentRS putTestCaseExecutionComment(TmsTestCaseExecution existingExecution,
      TmsTestCaseExecutionCommentRQ executionCommentRQ) {
    log.debug("Updating execution comment for execution: {}", existingExecution.getId());

    if (executionCommentRQ == null) {
      log.debug("No comment data provided, removing existing comment for execution: {}",
          existingExecution.getId());
      removeExistingComment(existingExecution);
      return null;
    }

    var existingComment = existingExecution.getExecutionComment();

    if (existingComment != null) {
      // Update existing comment
      return updateExistingComment(existingComment, executionCommentRQ);
    } else {
      // Create new comment
      return createNewComment(existingExecution, executionCommentRQ);
    }
  }

  @Override
  @Transactional
  public void deleteTestCaseExecutionComment(Long projectId, Long launchId, Long executionId) {
    if (!tmsTestCaseExecutionCommentRepository.existsByExecutionId(executionId)) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASE_EXECUTION_COMMENT_IN_EXECUTION.formatted(executionId, launchId)
      );
    }
    tmsTestCaseExecutionCommentRepository.deleteByExecutionId(executionId);
  }

  @Override
  @Transactional
  public void deleteByLaunchId(Long launchId) {
    tmsTestCaseExecutionCommentAttachmentService.deleteByLaunchId(launchId);
    tmsTestCaseExecutionCommentBtsTicketService.deleteByLaunchId(launchId);
    tmsTestCaseExecutionCommentRepository.deleteByLaunchId(launchId);
  }

  @Override
  @Transactional
  public void deleteTestCaseExecutionComment(long projectId, Long launchId,
      TmsTestCaseExecution execution) {
    var executionId = execution.getId();
    if (execution.getExecutionComment() == null || !tmsTestCaseExecutionCommentRepository.existsByExecutionId(executionId)) {
      throw new ReportPortalException(
          NOT_FOUND,
          TEST_CASE_EXECUTION_COMMENT_IN_EXECUTION.formatted(executionId, launchId)
      );
    }
    tmsTestCaseExecutionCommentRepository.delete(execution.getExecutionComment());
    execution.setExecutionComment(null);
  }

  /**
   * Updates existing execution comment with new data.
   *
   * @return
   */
  private TmsTestCaseExecutionCommentRS updateExistingComment(TmsTestCaseExecutionComment existingComment,
      TmsTestCaseExecutionCommentRQ executionCommentRQ) {
    log.debug("Updating existing comment: {}", existingComment.getId());

    // Update comment text
    existingComment.setComment(executionCommentRQ.getComment());

    // Update attachments (replace all existing with new ones)
    tmsTestCaseExecutionCommentAttachmentService.updateAttachments(existingComment,
        executionCommentRQ);

    // Update BTS tickets
    tmsTestCaseExecutionCommentBtsTicketService.updateBtsTickets(existingComment,
        executionCommentRQ);

    // Save updated comment
    return tmsTestCaseExecutionCommentMapper.toTmsTestCaseExecutionCommentRS(
        tmsTestCaseExecutionCommentRepository.save(existingComment)
    );
  }

  /**
   * Creates new execution comment.
   *
   * @return
   */
  private TmsTestCaseExecutionCommentRS createNewComment(TmsTestCaseExecution existingExecution,
      TmsTestCaseExecutionCommentRQ executionCommentRQ) {
    log.debug("Creating new comment for execution: {}", existingExecution.getId());

    // Create new comment entity
    var newComment = tmsTestCaseExecutionCommentMapper.createTestCaseExecutionComment(
        existingExecution, executionCommentRQ);

    // Save comment first to get ID for attachments
    var savedComment = tmsTestCaseExecutionCommentRepository.save(newComment);

    // Create attachments
    tmsTestCaseExecutionCommentAttachmentService.createAttachments(savedComment,
        executionCommentRQ);

    // Create BTS tickets
    tmsTestCaseExecutionCommentBtsTicketService.createBtsTickets(savedComment,
        executionCommentRQ);

    // Set bidirectional relationship
    existingExecution.setExecutionComment(savedComment);

    log.debug("Created new comment: {}", savedComment.getId());
    return tmsTestCaseExecutionCommentMapper.toTmsTestCaseExecutionCommentRS(
        savedComment
    );
  }

  /**
   * Removes existing execution comment and its attachments.
   */
  private void removeExistingComment(TmsTestCaseExecution existingExecution) {
    var existingComment = existingExecution.getExecutionComment();

    if (existingComment != null) {
      log.debug("Removing existing comment: {}", existingComment.getId());

      // Delete attachments first
      tmsTestCaseExecutionCommentAttachmentService.deleteAllByExecutionId(
          existingExecution.getId());

      // Delete BTS tickets
      tmsTestCaseExecutionCommentBtsTicketService.deleteAllByExecutionId(
          existingExecution.getId());

      // Delete comment
      tmsTestCaseExecutionCommentRepository.deleteById(existingComment.getId());

      // Clear relationship
      existingExecution.setExecutionComment(null);

      log.debug("Removed existing comment: {}", existingComment.getId());
    }
  }
}
