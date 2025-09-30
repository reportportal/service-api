package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsTextManualScenario;
import com.epam.ta.reportportal.dao.tms.TmsTextManualScenarioAttachmentRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
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
public class TmsTextManualScenarioAttachmentServiceImpl implements
    TmsTextManualScenarioAttachmentService {

  private final TmsAttachmentService tmsAttachmentService;
  private final TmsTextManualScenarioAttachmentRepository textManualScenarioAttachmentRepository;

  @Override
  @Transactional
  public void createAttachments(TmsTextManualScenario tmsTextManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ) {
    log.debug("Creating attachments for text manual scenario: {}",
        tmsTextManualScenario.getManualScenarioId());

    if (tmsTextManualScenarioRQ == null || CollectionUtils.isEmpty(
        tmsTextManualScenarioRQ.getAttachments())) {
      log.debug("No attachments to create for text manual scenario: {}",
          tmsTextManualScenario.getManualScenarioId());
      return;
    }

    var attachmentIds = tmsTextManualScenarioRQ
        .getAttachments()
        .stream()
        .map(attachment -> Long.valueOf(attachment.getId()))
        .collect(Collectors.toList());

    var attachments = tmsAttachmentService.getTmsAttachmentsByIds(attachmentIds);

    if (CollectionUtils.isNotEmpty(attachments)) {

      tmsTextManualScenario.setAttachments(new HashSet<>(attachments));

      attachments.forEach(attachment -> {
        if (attachment.getExpiresAt() != null) {
          attachment.setExpiresAt(null); //Remove TTL from attachment -> make that permanent
        }
        if (attachment.getTextManualScenarios() == null) {
          attachment.setTextManualScenarios(new HashSet<>());
        }
        attachment.getTextManualScenarios().add(tmsTextManualScenario);
      });

      tmsAttachmentService.saveAll(attachments);

      log.info("Created {} attachment relationships for text manual scenario: {}",
          attachments.size(), tmsTextManualScenario.getManualScenarioId());
    }
  }

  @Override
  @Transactional
  public void updateAttachments(TmsTextManualScenario textManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ) {
    log.debug("Updating attachments for text manual scenario: {}",
        textManualScenario.getManualScenarioId());

    // Delete existing relationships
   if (CollectionUtils.isNotEmpty(textManualScenario.getAttachments())) {
     textManualScenarioAttachmentRepository
         .deleteByTextManualScenarioId(textManualScenario.getManualScenarioId());
     textManualScenario.setAttachments(new HashSet<>());
     log.debug("Deleted existing attachment relationships for text manual scenario: {}",
         textManualScenario.getManualScenarioId());
   }

    // Create new relationships
    createAttachments(textManualScenario, tmsTextManualScenarioRQ);
  }

  @Override
  @Transactional
  public void patchAttachments(TmsTextManualScenario existingTextManualScenario,
      TmsTextManualScenarioRQ tmsTextManualScenarioRQ) {
    log.debug("Patching attachments for text manual scenario: {}",
        existingTextManualScenario.getManualScenarioId());

    if (tmsTextManualScenarioRQ == null || CollectionUtils.isEmpty(
        tmsTextManualScenarioRQ.getAttachments())) {
      log.debug("No attachments to patch for text manual scenario: {}",
          existingTextManualScenario.getManualScenarioId());
      return;
    }

    var newAttachmentIds = tmsTextManualScenarioRQ
        .getAttachments()
        .stream()
        .map(attachment -> Long.valueOf(attachment.getId()))
        .collect(Collectors.toList());

    var newAttachments = tmsAttachmentService.getTmsAttachmentsByIds(newAttachmentIds);

    if (CollectionUtils.isNotEmpty(newAttachments)) {
      if (existingTextManualScenario.getAttachments()
          == null) { //Remove TTL from attachment -> make that permanent
        existingTextManualScenario.setAttachments(new HashSet<>());
      }
      existingTextManualScenario.getAttachments().addAll(newAttachments);

      newAttachments.forEach(attachment -> {
        if (attachment.getExpiresAt() != null) {
          attachment.setExpiresAt(null);
        }
        if (attachment.getTextManualScenarios() == null) {
          attachment.setManualScenarioPreconditions(new HashSet<>());
        }
        attachment.getTextManualScenarios().add(existingTextManualScenario);
      });

      tmsAttachmentService.saveAll(newAttachments);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    log.debug("Deleting all text manual scenario attachment relationships by test case ID: {}",
        testCaseId);

    if (testCaseId == null) {
      log.warn("Test case ID is null, skipping delete operation");
      return;
    }

    textManualScenarioAttachmentRepository.deleteByTestCaseId(testCaseId);
    log.info("Deleted all text manual scenario attachment relationships for test case: {}",
        testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    log.debug("Deleting all text manual scenario attachment relationships by test case IDs: {}",
        testCaseIds);

    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("Test case IDs list is empty, skipping delete operation");
      return;
    }

    textManualScenarioAttachmentRepository.deleteByTestCaseIds(testCaseIds);
    log.info("Deleted all text manual scenario attachment relationships for {} test cases",
        testCaseIds.size());
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    log.debug(
        "Deleting all text manual scenario attachment relationships by project: {} and folder: {}",
        projectId, folderId);

    if (projectId == null || folderId == null) {
      log.warn("Project ID or folder ID is null, skipping delete operation");
      return;
    }

    textManualScenarioAttachmentRepository.deleteByTestFolderId(projectId, folderId);
    log.info(
        "Deleted all text manual scenario attachment relationships for project: {} and folder: {}",
        projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateAttachments(TmsTextManualScenario originalTextScenario,
      TmsTextManualScenario duplicatedTextScenario) {
    log.debug("Duplicating attachment relationships from text manual scenario: {} to: {}",
        originalTextScenario.getManualScenarioId(), duplicatedTextScenario.getManualScenarioId());

    // Get original relationships
    var originalTextManualScenarioAttachments = originalTextScenario.getAttachments();

    if (CollectionUtils.isEmpty(originalTextManualScenarioAttachments)) {
      log.debug("No attachment relationships to duplicate for text manual scenario: {}",
          originalTextScenario.getManualScenarioId());
      return;
    }

    originalTextManualScenarioAttachments.forEach(originalAttachment -> {
      var duplicatedAttachment = tmsAttachmentService.duplicateTmsAttachment(originalAttachment);

      if (duplicatedTextScenario.getAttachments() == null) {
        duplicatedTextScenario.setAttachments(new HashSet<>());
      }
      if (duplicatedAttachment.getTextManualScenarios() == null) {
        duplicatedAttachment.setTextManualScenarios(new HashSet<>());
      }
      duplicatedTextScenario.getAttachments().add(duplicatedAttachment);
      duplicatedAttachment.getTextManualScenarios().add(duplicatedTextScenario);
    });
  }
}
