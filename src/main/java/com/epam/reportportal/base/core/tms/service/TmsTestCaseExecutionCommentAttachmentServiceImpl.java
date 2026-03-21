package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseExecutionCommentAttachmentRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionComment;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecutionCommentAttachment;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
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
public class TmsTestCaseExecutionCommentAttachmentServiceImpl implements
    TmsTestCaseExecutionCommentAttachmentService {

  private final TmsAttachmentService tmsAttachmentService;
  private final TmsTestCaseExecutionCommentAttachmentRepository tmsTestCaseExecutionCommentAttachmentRepository;

  @Override
  @Transactional
  public void createAttachments(TmsTestCaseExecutionComment tmsTestCaseExecutionComment,
      TmsTestCaseExecutionCommentRQ commentRQ) {
    log.debug("Creating attachments for execution tmsTestCaseExecutionComment: {}",
        tmsTestCaseExecutionComment.getId());

    if (commentRQ == null || CollectionUtils.isEmpty(commentRQ.getAttachments())) {
      log.debug("No attachments to create for execution tmsTestCaseExecutionComment: {}",
          tmsTestCaseExecutionComment.getId());
      return;
    }

    var attachmentIds = commentRQ
        .getAttachments()
        .stream()
        .map(attachment -> Long.valueOf(attachment.getId()))
        .collect(Collectors.toList());

    // Validate and get attachments
    var attachments = tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds);

    if (CollectionUtils.isNotEmpty(attachments)) {
      tmsTestCaseExecutionComment.setAttachments(new HashSet<>());

      attachments.forEach(attachment -> {
        if (attachment.getExpiresAt() != null) {
          attachment.setExpiresAt(null); // Remove TTL from attachment -> make that permanent
        }
        if (attachment.getExecutionComments() == null) {
          attachment.setExecutionComments(new HashSet<>());
        }

        tmsTestCaseExecutionComment.getAttachments().add(attachment);
        attachment.getExecutionComments().add(tmsTestCaseExecutionComment);
      });

      log.debug("Created {} attachment relationships for execution tmsTestCaseExecutionComment: {}",
          attachments.size(), tmsTestCaseExecutionComment.getId());
    } else {
      throw new ReportPortalException(ErrorType.NOT_FOUND, "No attachments found with such ids");
    }
  }

  @Override
  @Transactional
  public void updateAttachments(TmsTestCaseExecutionComment existingComment,
      TmsTestCaseExecutionCommentRQ commentRQ) {
    log.debug("Updating attachments for execution comment: {}", existingComment.getId());

    // Delete existing relationships
    if (CollectionUtils.isNotEmpty(existingComment.getAttachments())) {
      tmsTestCaseExecutionCommentAttachmentRepository
          .deleteByExecutionCommentId(existingComment.getId());
      existingComment.setAttachments(new HashSet<>());
      log.debug("Deleted existing attachment relationships for execution comment: {}",
          existingComment.getId());
    }

    // Create new relationships
    createAttachments(existingComment, commentRQ);
  }

  @Override
  @Transactional
  public void deleteAllByExecutionId(Long executionId) {
    log.debug("Deleting all execution comment attachment relationships by execution ID: {}",
        executionId);

    if (executionId == null) {
      log.warn("Execution ID is null, skipping delete operation");
      return;
    }

    tmsTestCaseExecutionCommentAttachmentRepository.deleteByExecutionId(executionId);
    log.debug("Deleted all execution comment attachment relationships for execution: {}",
        executionId);
  }

  @Override
  @Transactional
  public void deleteByLaunchId(Long launchId) {
    tmsTestCaseExecutionCommentAttachmentRepository.deleteByLaunchId(launchId);
  }
}
