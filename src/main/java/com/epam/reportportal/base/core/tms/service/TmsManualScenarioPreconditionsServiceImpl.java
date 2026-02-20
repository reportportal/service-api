package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioPreconditionRepository;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioPreconditionsMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsManualScenarioPreconditionsServiceImpl implements
    TmsManualScenarioPreconditionsService {

  private final TmsManualScenarioPreconditionsAttachmentService tmsManualScenarioPreconditionsAttachmentService;
  private final TmsManualScenarioPreconditionRepository tmsManualScenarioPreconditionRepository;
  private final TmsManualScenarioPreconditionsMapper tmsManualScenarioPreconditionsMapper;

  @Override
  @Transactional
  public void createPreconditions(TmsManualScenario tmsManualScenario,
      TmsManualScenarioPreconditionsRQ tmsManualScenarioPreconditionsRQ) {
    log.debug("Creating preconditions for manual scenario: {}", tmsManualScenario.getId());

    if (tmsManualScenarioPreconditionsRQ == null) {
      log.debug("No preconditions data provided for manual scenario: {}",
          tmsManualScenario.getId());
      return;
    }

    // Map DTO to entity
    var preconditions = tmsManualScenarioPreconditionsMapper.toEntity(
        tmsManualScenarioPreconditionsRQ);

    preconditions.setManualScenario(tmsManualScenario);

    // Save preconditions entity
    var savedPreconditions = tmsManualScenarioPreconditionRepository.save(preconditions);

    tmsManualScenario.setPreconditions(savedPreconditions);

    // Create attachments if present
    tmsManualScenarioPreconditionsAttachmentService.createAttachments(savedPreconditions,
        tmsManualScenarioPreconditionsRQ);

    log.debug("Created preconditions with ID: {} for manual scenario: {}",
        savedPreconditions.getId(), tmsManualScenario.getId());
  }

  @Override
  @Transactional
  public void updatePreconditions(TmsManualScenario manualScenario,
      TmsManualScenarioPreconditionsRQ tmsManualScenarioPreconditionsRQ) {
    log.debug("Updating preconditions for manual scenario: {}", manualScenario.getId());

    // Delete existing preconditions if any
    var existingPreconditions = manualScenario.getPreconditions();
    if (existingPreconditions != null) {
      log.debug("Deleting existing preconditions: {}", existingPreconditions.getId());
      if (tmsManualScenarioPreconditionsRQ == null) {
        tmsManualScenarioPreconditionRepository.deleteById(existingPreconditions.getId());
        manualScenario.setPreconditions(null);
      } else {
        tmsManualScenarioPreconditionsMapper.update(
            existingPreconditions, tmsManualScenarioPreconditionsRQ
        );
        tmsManualScenarioPreconditionsAttachmentService.updateAttachments(existingPreconditions,
            tmsManualScenarioPreconditionsRQ);
      }
    } else {
      // Create new preconditions if provided
      if (tmsManualScenarioPreconditionsRQ != null) {
        createPreconditions(manualScenario, tmsManualScenarioPreconditionsRQ);
      }
    }

    log.debug("Updated preconditions for manual scenario: {}", manualScenario.getId());
  }

  @Override
  @Transactional
  public void patchPreconditions(TmsManualScenario existingManualScenario,
      TmsManualScenarioPreconditionsRQ tmsManualScenarioPreconditionsRQ) {
    log.debug("Patching preconditions for manual scenario: {}", existingManualScenario.getId());

    if (tmsManualScenarioPreconditionsRQ == null) {
      log.debug("No preconditions data provided for patching manual scenario: {}",
          existingManualScenario.getId());
      return;
    }

    var existingPreconditions = existingManualScenario.getPreconditions();

    // If no existing preconditions, create new ones
    if (existingPreconditions == null) {
      log.debug("No existing preconditions found, creating new ones for manual scenario: {}",
          existingManualScenario.getId());
      createPreconditions(existingManualScenario, tmsManualScenarioPreconditionsRQ);
      return;
    }

    // Update existing preconditions with non-null values from request
    tmsManualScenarioPreconditionsMapper.patch(existingPreconditions,
        tmsManualScenarioPreconditionsRQ);
    var savedPreconditions = tmsManualScenarioPreconditionRepository.save(existingPreconditions);

    tmsManualScenarioPreconditionsAttachmentService.updateAttachments(savedPreconditions,
        tmsManualScenarioPreconditionsRQ);

    log.debug("Patched preconditions with ID: {} for manual scenario: {}",
        savedPreconditions.getId(), existingManualScenario.getId());
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    log.debug("Deleting all preconditions by test case ID: {}", testCaseId);

    if (testCaseId == null) {
      log.warn("Test case ID is null, skipping delete operation");
      return;
    }

    // First delete attachments
    tmsManualScenarioPreconditionsAttachmentService.deleteAllByTestCaseId(testCaseId);

    // Then delete preconditions
    tmsManualScenarioPreconditionRepository.deleteAllByTestCaseId(testCaseId);

    log.debug("Deleted all preconditions for test case: {}", testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    log.debug("Deleting all preconditions by test case IDs: {}", testCaseIds);

    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("Test case IDs list is empty, skipping delete operation");
      return;
    }

    // First delete attachments
    tmsManualScenarioPreconditionsAttachmentService.deleteAllByTestCaseIds(testCaseIds);

    // Then delete preconditions
    tmsManualScenarioPreconditionRepository.deleteAllByTestCaseIds(testCaseIds);

    log.debug("Deleted all preconditions for {} test cases", testCaseIds.size());
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    log.debug("Deleting all preconditions by project: {} and folder: {}", projectId, folderId);

    if (projectId == null || folderId == null) {
      log.warn("Project ID or folder ID is null, skipping delete operation");
      return;
    }

    // First delete attachments
    tmsManualScenarioPreconditionsAttachmentService.deleteAllByTestFolderId(projectId, folderId);

    // Then delete preconditions
    tmsManualScenarioPreconditionRepository.deleteAllByTestFolderId(projectId, folderId);

    log.debug("Deleted all preconditions for project: {} and folder: {}", projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicatePreconditions(TmsManualScenario originalScenario,
      TmsManualScenario duplicatedScenario) {
    log.debug("Duplicating preconditions from manual scenario: {} to: {}",
        originalScenario.getId(), duplicatedScenario.getId());

    var originalPreconditions = originalScenario.getPreconditions();
    if (originalPreconditions == null) {
      log.debug("No preconditions to duplicate for manual scenario: {}", originalScenario.getId());
      return;
    }

    // Create duplicated preconditions entity
    var duplicatedPreconditions = tmsManualScenarioPreconditionsMapper.duplicate(
        originalPreconditions);
    duplicatedPreconditions.setManualScenario(duplicatedScenario);

    // Save duplicated preconditions
    var savedDuplicatedPreconditions = tmsManualScenarioPreconditionRepository.save(
        duplicatedPreconditions);

    // Duplicate attachments
    tmsManualScenarioPreconditionsAttachmentService.duplicateAttachments(
        originalPreconditions, savedDuplicatedPreconditions);

    duplicatedScenario.setPreconditions(savedDuplicatedPreconditions);

    log.debug("Duplicated preconditions from manual scenario: {} to: {} with new ID: {}",
        originalScenario.getId(), duplicatedScenario.getId(),
        savedDuplicatedPreconditions.getId());
  }
}
