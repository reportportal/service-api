package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioPreconditions;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioPreconditionsAttachmentRepository;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
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
public class TmsManualScenarioPreconditionsAttachmentServiceImpl implements
    TmsManualScenarioPreconditionsAttachmentService {

  private final TmsAttachmentService tmsAttachmentService;
  private final TmsManualScenarioPreconditionsAttachmentRepository preconditionsAttachmentRepository;

  @Override
  @Transactional
  public void createAttachments(TmsManualScenarioPreconditions preconditions,
      TmsManualScenarioPreconditionsRQ preconditionsRQ) {
    log.debug("Creating attachments for manual scenario preconditions: {}", preconditions.getId());

    if (preconditionsRQ == null || CollectionUtils.isEmpty(preconditionsRQ.getAttachments())) {
      log.debug("No attachments to create for preconditions: {}", preconditions.getId());
      return;
    }

    var attachmentIds = preconditionsRQ
        .getAttachments()
        .stream()
        .map(attachment -> Long.valueOf(attachment.getId()))
        .collect(Collectors.toList());

    // Validate and get attachments
    var attachments = tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds);

    if (CollectionUtils.isNotEmpty(attachments)) {
      preconditions.setAttachments(new HashSet<>(attachments));

      attachments.forEach(attachment -> {
        if (attachment.getExpiresAt() != null) {
          attachment.setExpiresAt(null); //Remove TTL from attachment -> make that permanent
        }
        if (attachment.getManualScenarioPreconditions() == null) {
          attachment.setManualScenarioPreconditions(new HashSet<>());
        }
        attachment.getManualScenarioPreconditions().add(preconditions);
      });

      tmsAttachmentService.saveAll(attachments);

      log.debug("Created {} attachment relationships for preconditions: {}", attachments.size(),
          preconditions.getId());
    }
  }

  @Override
  @Transactional
  public void updateAttachments(TmsManualScenarioPreconditions existingPreconditions,
      TmsManualScenarioPreconditionsRQ tmsManualScenarioPreconditionsRQ) {
    log.debug("Updating attachments for preconditions: {}",
        existingPreconditions);

    // Delete existing relationships
    if (CollectionUtils.isNotEmpty(existingPreconditions.getAttachments())) {
      preconditionsAttachmentRepository
          .deleteByPreconditionsId(existingPreconditions.getId());
      existingPreconditions.setAttachments(new HashSet<>());
      log.debug("Deleted existing attachment relationships for preconditions: {}",
          existingPreconditions.getId());
    }

    // Create new relationships
    createAttachments(existingPreconditions, tmsManualScenarioPreconditionsRQ);
  }

  @Override
  @Transactional
  public void patchAttachments(TmsManualScenarioPreconditions existingPreconditions,
      TmsManualScenarioPreconditionsRQ preconditionsRQ) {
    log.debug("Patching attachments for preconditions: {}", existingPreconditions.getId());

    if (preconditionsRQ == null || CollectionUtils.isEmpty(preconditionsRQ.getAttachments())) {
      log.debug("No attachments to patch for preconditions: {}", existingPreconditions.getId());
      return;
    }

    var newAttachmentIds = preconditionsRQ
        .getAttachments()
        .stream()
        .map(attachment -> Long.valueOf(attachment.getId()))
        .collect(Collectors.toList());

    // Validate and get new attachments
    var newAttachments = tmsAttachmentService.getTmsAttachmentsByIds(newAttachmentIds);

    if (CollectionUtils.isNotEmpty(newAttachments)) {
      if (existingPreconditions.getAttachments() == null) { //Remove TTL from attachment -> make that permanent
        existingPreconditions.setAttachments(new HashSet<>());
      }
      existingPreconditions.getAttachments().addAll(newAttachments);

      newAttachments.forEach(attachment -> {
        if (attachment.getExpiresAt() != null) {
          attachment.setExpiresAt(null);
        }
        if (attachment.getManualScenarioPreconditions() == null) {
          attachment.setManualScenarioPreconditions(new HashSet<>());
        }
        attachment.getManualScenarioPreconditions().add(existingPreconditions);
      });

      tmsAttachmentService.saveAll(newAttachments);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    log.debug("Deleting all preconditions attachment relationships by test case ID: {}",
        testCaseId);

    if (testCaseId == null) {
      log.warn("Test case ID is null, skipping delete operation");
      return;
    }

    preconditionsAttachmentRepository.deleteByTestCaseId(testCaseId);
    log.debug("Deleted all preconditions attachment relationships for test case: {}", testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    log.debug("Deleting all preconditions attachment relationships by test case IDs: {}",
        testCaseIds);

    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("Test case IDs list is empty, skipping delete operation");
      return;
    }

    preconditionsAttachmentRepository.deleteByTestCaseIds(testCaseIds);
    log.debug("Deleted all preconditions attachment relationships for {} test cases",
        testCaseIds.size());
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    log.debug("Deleting all preconditions attachment relationships by project: {} and folder: {}",
        projectId, folderId);

    if (projectId == null || folderId == null) {
      log.warn("Project ID or folder ID is null, skipping delete operation");
      return;
    }

    preconditionsAttachmentRepository.deleteByTestFolderId(projectId, folderId);
    log.debug("Deleted all preconditions attachment relationships for project: {} and folder: {}",
        projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateAttachments(TmsManualScenarioPreconditions originalPreconditions,
      TmsManualScenarioPreconditions duplicatedPreconditions) {
    log.debug("Duplicating attachment relationships from preconditions: {} to: {}",
        originalPreconditions.getId(), duplicatedPreconditions.getId());

    // Get original relationships
    var originalPreconditionAttachments = originalPreconditions.getAttachments();

    if (CollectionUtils.isEmpty(originalPreconditionAttachments)) {
      log.debug("No attachment relationships to duplicate for preconditions: {}",
          originalPreconditions.getId());
      return;
    }

    originalPreconditionAttachments.forEach(originalAttachment -> {
      var duplicatedAttachment = tmsAttachmentService.duplicateTmsAttachment(originalAttachment);

      if (duplicatedPreconditions.getAttachments() == null) {
        duplicatedPreconditions.setAttachments(new HashSet<>());
      }
      if (duplicatedAttachment.getManualScenarioPreconditions() == null) {
        duplicatedAttachment.setManualScenarioPreconditions(new HashSet<>());
      }
      duplicatedPreconditions.getAttachments().add(duplicatedAttachment);
      duplicatedAttachment.getManualScenarioPreconditions().add(duplicatedPreconditions);
    });
  }
}
