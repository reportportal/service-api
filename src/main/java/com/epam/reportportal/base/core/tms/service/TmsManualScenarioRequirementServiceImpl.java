package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsManualScenarioRequirementMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsManualScenarioRequirementRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenarioRequirement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmsManualScenarioRequirementServiceImpl implements
    TmsManualScenarioRequirementService {

  private final TmsManualScenarioRequirementRepository tmsManualScenarioRequirementRepository;
  private final TmsManualScenarioRequirementMapper tmsManualScenarioRequirementMapper;

  @Override
  @Transactional
  public void createRequirements(TmsManualScenario tmsManualScenario,
      List<TmsRequirementRQ> requirements) {
    log.debug("Creating requirements for manual scenario: {}", tmsManualScenario.getId());

    if (CollectionUtils.isEmpty(requirements)) {
      log.debug("No requirements data provided for manual scenario: {}",
          tmsManualScenario.getId());
      return;
    }

    var entities = new ArrayList<TmsManualScenarioRequirement>();
    for (int i = 0; i < requirements.size(); i++) {
      var requirementRQ = requirements.get(i);
      var existingOpt = tmsManualScenarioRequirementRepository.findById(requirementRQ.getId());
      if (existingOpt.isPresent()) {
        var existing = existingOpt.get();
        existing.setValue(requirementRQ.getValue());
        existing.setManualScenario(tmsManualScenario);
        existing.setNumber(i);
        entities.add(existing);
      } else {
        var entity = tmsManualScenarioRequirementMapper.toEntity(requirementRQ);
        entity.setManualScenario(tmsManualScenario);
        entity.setNumber(i);
        entities.add(entity);
      }
    }

    tmsManualScenario.setRequirements(tmsManualScenarioRequirementRepository.saveAll(entities));

    log.debug("Created {} requirements for manual scenario: {}",
        entities.size(), tmsManualScenario.getId());
  }

  @Override
  @Transactional
  public void updateRequirements(TmsManualScenario manualScenario,
      List<TmsRequirementRQ> requirements) {
    log.debug("Updating requirements for manual scenario: {}", manualScenario.getId());

    // Delete all existing requirements for this manual scenario
    if (CollectionUtils.isNotEmpty(manualScenario.getRequirements())) {
      tmsManualScenarioRequirementRepository.deleteAll(
        manualScenario.getRequirements()
      );
      manualScenario.setRequirements(null);
    }

    if (CollectionUtils.isNotEmpty(requirements)) {
      createRequirements(manualScenario, requirements);
    }

    log.debug("Updated requirements for manual scenario: {}", manualScenario.getId());
  }

  @Override
  @Transactional
  public void patchRequirements(TmsManualScenario existingManualScenario,
      List<TmsRequirementRQ> requirements) {
    log.debug("Patching requirements for manual scenario: {}", existingManualScenario.getId());

    if (CollectionUtils.isEmpty(requirements)) {
      log.debug("No requirements data provided for patching manual scenario: {}",
          existingManualScenario.getId());
      return;
    }

    // Build a map of existing requirements by id for efficient lookup
    var existingRequirementsList = tmsManualScenarioRequirementRepository
        .findByManualScenarioIdOrderByNumberAsc(existingManualScenario.getId());

    var existingRequirements = existingRequirementsList.stream()
        .collect(Collectors.toMap(TmsManualScenarioRequirement::getId, Function.identity()));

    int maxNumber = existingRequirementsList.stream()
        .mapToInt(TmsManualScenarioRequirement::getNumber)
        .max()
        .orElse(-1);

    var entitiesToSave = new ArrayList<TmsManualScenarioRequirement>();
    for (var requirementRQ : requirements) {
      var existing = existingRequirements.get(requirementRQ.getId());
      if (existing != null) {
        // If requirement with this id already exists — update only its value, preserve number
        existing.setValue(requirementRQ.getValue());
        entitiesToSave.add(existing);
      } else {
        // Check if requirement exists globally (different scenario)
        var globalOpt = tmsManualScenarioRequirementRepository.findById(requirementRQ.getId());
        if (globalOpt.isPresent()) {
          var global = globalOpt.get();
          global.setValue(requirementRQ.getValue());
          global.setManualScenario(existingManualScenario);
          global.setNumber(++maxNumber);
          entitiesToSave.add(global);
        } else {
          var entity = tmsManualScenarioRequirementMapper.toEntity(requirementRQ);
          entity.setManualScenario(existingManualScenario);
          entity.setNumber(++maxNumber);
          entitiesToSave.add(entity);
        }
      }
    }

    existingManualScenario
        .getRequirements()
        .addAll(tmsManualScenarioRequirementRepository.saveAll(entitiesToSave));

    log.debug("Patched {} requirements for manual scenario: {}",
        entitiesToSave.size(), existingManualScenario.getId());
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    log.debug("Deleting all requirements by test case ID: {}", testCaseId);

    if (testCaseId == null) {
      log.warn("Test case ID is null, skipping delete operation");
      return;
    }

    tmsManualScenarioRequirementRepository.deleteAllByTestCaseId(testCaseId);

    log.debug("Deleted all requirements for test case: {}", testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    log.debug("Deleting all requirements by test case IDs: {}", testCaseIds);

    if (CollectionUtils.isEmpty(testCaseIds)) {
      log.debug("Test case IDs list is empty, skipping delete operation");
      return;
    }

    tmsManualScenarioRequirementRepository.deleteAllByTestCaseIds(testCaseIds);

    log.debug("Deleted all requirements for {} test cases", testCaseIds.size());
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    log.debug("Deleting all requirements by project: {} and folder: {}", projectId, folderId);

    if (projectId == null || folderId == null) {
      log.warn("Project ID or folder ID is null, skipping delete operation");
      return;
    }

    tmsManualScenarioRequirementRepository.deleteAllByTestFolderId(projectId, folderId);

    log.debug("Deleted all requirements for project: {} and folder: {}", projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateRequirements(TmsManualScenario originalScenario,
      TmsManualScenario duplicatedScenario) {
    log.debug("Duplicating requirements from manual scenario: {} to: {}",
        originalScenario.getId(), duplicatedScenario.getId());

    var originalRequirements = originalScenario.getRequirements();
    if (CollectionUtils.isEmpty(originalRequirements)) {
      log.debug("No requirements to duplicate for manual scenario: {}",
          originalScenario.getId());
      return;
    }

    var duplicatedEntities = originalRequirements.stream()
        .sorted(Comparator.comparing(TmsManualScenarioRequirement::getNumber))
        .map(original -> {
          var duplicated = tmsManualScenarioRequirementMapper.duplicate(original);
          duplicated.setManualScenario(duplicatedScenario);
          return duplicated;
        })
        .toList();

    tmsManualScenarioRequirementRepository.saveAll(duplicatedEntities);

    log.debug("Duplicated {} requirements from manual scenario: {} to: {}",
        duplicatedEntities.size(), originalScenario.getId(), duplicatedScenario.getId());
  }
}
