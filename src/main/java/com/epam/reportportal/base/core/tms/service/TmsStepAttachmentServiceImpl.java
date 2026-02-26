package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStep;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsStepAttachmentRepository;
import com.epam.reportportal.base.core.tms.dto.TmsStepRQ;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsStepAttachmentServiceImpl implements TmsStepAttachmentService {

  private final TmsAttachmentService tmsAttachmentService;
  private final TmsStepAttachmentRepository tmsStepAttachmentRepository;

  @Override
  @Transactional
  public void createAttachments(TmsStep tmsStep, TmsStepRQ stepRQ) {
    log.debug("Creating attachments for step: {}", tmsStep.getId());

    if (stepRQ == null || CollectionUtils.isEmpty(stepRQ.getAttachments())) {
      log.debug("No attachments to create for step: {}", tmsStep.getId());
      return;
    }

    var attachmentIds = stepRQ
        .getAttachments()
        .stream()
        .map(attachment -> Long.valueOf(attachment.getId()))
        .collect(Collectors.toList());

    // Validate and get attachments
    var attachments = tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds);

    if (CollectionUtils.isNotEmpty(attachments)) {

      tmsStep.setAttachments(new HashSet<>(attachments));

      attachments.forEach(attachment -> {
        if (attachment.getExpiresAt() != null) {
          attachment.setExpiresAt(null); //Remove TTL from attachment -> make that permanent
        }
        if (attachment.getSteps() == null) {
          attachment.setSteps(new HashSet<>());
        }
        attachment.getSteps().add(tmsStep);
      });
      tmsAttachmentService.saveAll(attachments);

      log.info("Created {} attachment relationships for step: {}", attachments.size(),
          tmsStep.getId());
    }
  }

  @Override
  @Transactional
  public void deleteAllBySteps(Collection<TmsStep> steps) {
    if (CollectionUtils.isEmpty(steps)) {
      log.debug("No steps provided, skipping delete operation");
      return;
    }

    var stepIds = steps.stream()
        .map(TmsStep::getId)
        .collect(Collectors.toList());

    tmsStepAttachmentRepository.deleteByStepIdIn(stepIds);
    log.info("Deleted all attachment relationships for {} steps", stepIds.size());
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    log.debug("Deleting all step attachment relationships by test case ID: {}", testCaseId);

    if (testCaseId == null) {
      log.warn("Test case ID is null, skipping delete operation");
      return;
    }

    tmsStepAttachmentRepository.deleteByTestCaseId(testCaseId);
    log.info("Deleted all step attachment relationships for test case: {}", testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    log.debug("Deleting all step attachment relationships by test case IDs: {}", testCaseIds);

    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("Test case IDs list is empty, skipping delete operation");
      return;
    }

    tmsStepAttachmentRepository.deleteByTestCaseIds(testCaseIds);
    log.info("Deleted all step attachment relationships for {} test cases", testCaseIds.size());
  }

  @Override
  @Transactional
  public void deleteStepsByTestFolderId(Long projectId, Long folderId) {
    log.debug("Deleting all step attachment relationships by project: {} and folder: {}",
        projectId, folderId);

    if (projectId == null || folderId == null) {
      log.warn("Project ID or folder ID is null, skipping delete operation");
      return;
    }

    tmsStepAttachmentRepository.deleteByTestFolderId(projectId, folderId);
    log.info("Deleted all step attachment relationships for project: {} and folder: {}",
        projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateAttachments(TmsStep originalStep, TmsStep duplicatedStep) {
    log.debug("Duplicating attachment relationships from step: {} to: {}",
        originalStep.getId(), duplicatedStep.getId());

    // Get original relationships
    var originalStepAttachments = originalStep.getAttachments();

    if (CollectionUtils.isEmpty(originalStepAttachments)) {
      log.debug("No attachment relationships to duplicate for step: {}", originalStep.getId());
      return;
    }

    originalStepAttachments.forEach(originalAttachment -> {
      var duplicatedAttachment = tmsAttachmentService.duplicateTmsAttachment(originalAttachment);

      if (duplicatedStep.getAttachments() == null) {
        duplicatedStep.setAttachments(new HashSet<>());
      }
      if (duplicatedAttachment.getSteps() == null) {
        duplicatedAttachment.setSteps(new HashSet<>());
      }
      duplicatedStep.getAttachments().add(duplicatedAttachment);
      duplicatedAttachment.getSteps().add(duplicatedStep);
    });
  }
}
