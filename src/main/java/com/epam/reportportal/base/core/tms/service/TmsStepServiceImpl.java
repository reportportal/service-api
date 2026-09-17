package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsStepMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsStepRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStep;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepsManualScenario;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsStepServiceImpl implements TmsStepService {

  private final TmsStepMapper tmsStepMapper;
  private final TmsStepRepository tmsStepRepository;
  private final TmsStepAttachmentService tmsStepAttachmentService;

  @Override
  @Transactional
  public void createSteps(Long projectId, TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRq) {
    if (CollectionUtils.isEmpty(testCaseManualScenarioRq.getSteps())) {
      return;
    }

    var stepsRqs = testCaseManualScenarioRq.getSteps();
    if (CollectionUtils.isEmpty(stepsRqs)) {
      return;
    }

    var createdSteps = new HashSet<TmsStep>();
    for (var i = 0; i < stepsRqs.size(); i++) {
      var stepRq = stepsRqs.get(i);
      var tmsStep = tmsStepMapper.convertToTmsStep(stepRq);

      tmsStep.setNumber(i);
      tmsStep.setStepsManualScenario(tmsManualScenario);

      tmsStepRepository.save(tmsStep);
      createdSteps.add(tmsStep);

      tmsStepAttachmentService.createAttachments(projectId, tmsStep, stepRq);
    }

    if (tmsManualScenario.getSteps() == null) {
      tmsManualScenario.setSteps(new HashSet<>());
    }
    tmsManualScenario.getSteps().addAll(createdSteps);
  }

  @Override
  @Transactional
  public void updateSteps(Long projectId, TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRq) {
    if (CollectionUtils.isNotEmpty(tmsManualScenario.getSteps())) {
      tmsStepAttachmentService.deleteAllBySteps(tmsManualScenario.getSteps());
      tmsStepRepository.deleteAll(tmsManualScenario.getSteps());
      tmsManualScenario.setSteps(new HashSet<>());
    }

    createSteps(projectId, tmsManualScenario, testCaseManualScenarioRq);
  }

  @Override
  @Transactional
  public void patchSteps(Long projectId, TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRq) {
    if (testCaseManualScenarioRq == null || testCaseManualScenarioRq.getSteps() == null) {
      return;
    }

    updateSteps(projectId, tmsManualScenario, testCaseManualScenarioRq);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsStepAttachmentService.deleteAllByTestCaseId(testCaseId);
    tmsStepRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      tmsStepAttachmentService.deleteAllByTestCaseIds(testCaseIds);
      tmsStepRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsStepAttachmentService.deleteStepsByTestFolderId(projectId, folderId);
    tmsStepRepository.deleteStepsByTestFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateSteps(Collection<TmsStep> originalSteps,
      TmsStepsManualScenario newStepsScenario) {
    if (CollectionUtils.isEmpty(originalSteps)) {
      return;
    }

    // Convert to list and sort by number to preserve order
    var sortedOriginalSteps = originalSteps
        .stream()
        .sorted(Comparator.comparingInt(TmsStep::getNumber))
        .toList();

    var duplicatedSteps = sortedOriginalSteps
        .stream()
        .map(originalStep -> {
          var duplicatedStep = tmsStepMapper.duplicateStep(originalStep, newStepsScenario);

          // Preserve the number from original step
          duplicatedStep.setNumber(originalStep.getNumber());

          if (CollectionUtils.isNotEmpty(originalStep.getAttachments())) {
            tmsStepAttachmentService.duplicateAttachments(originalStep, duplicatedStep);
          }

          return duplicatedStep;
        })
        .collect(Collectors.toSet());

    newStepsScenario.setSteps(duplicatedSteps);
    tmsStepRepository.saveAll(duplicatedSteps);
  }
}
