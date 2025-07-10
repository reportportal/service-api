package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsStepMapper;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsStepServiceImpl implements TmsStepService {

  private final TmsStepMapper tmsStepMapper;
  private final TmsStepRepository tmsStepRepository;

  @Override
  @Transactional
  public void createStep(TmsManualScenario tmsManualScenario,
      TmsTextManualScenarioRQ testCaseManualScenarioRQ) {
    var step = tmsStepMapper.convertToTmsStep(testCaseManualScenarioRQ);

    tmsManualScenario.setSteps(Collections.singleton(step));
    step.setManualScenario(tmsManualScenario);

    tmsStepRepository.save(step);
  }

  @Override
  @Transactional
  public void createSteps(TmsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    if (CollectionUtils.isEmpty(testCaseManualScenarioRQ.getSteps())) {
      return;
    }

    var steps = tmsStepMapper.convertToTmsSteps(testCaseManualScenarioRQ);

    tmsManualScenario.setSteps(steps);
    steps.forEach(step -> step.setManualScenario(tmsManualScenario));

    tmsStepRepository.saveAll(steps);
  }

  @Override
  @Transactional
  public void updateStep(TmsManualScenario tmsManualScenario,
      TmsTextManualScenarioRQ testCaseManualScenarioRQ) {
    if (CollectionUtils.isNotEmpty(tmsManualScenario.getSteps())) {
      tmsStepRepository.deleteAll(tmsManualScenario.getSteps());
    }

    createStep(tmsManualScenario, testCaseManualScenarioRQ);
  }

  @Override
  @Transactional
  public void updateSteps(TmsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    if (CollectionUtils.isNotEmpty(tmsManualScenario.getSteps())) {
      tmsStepRepository.deleteAll(tmsManualScenario.getSteps());
      tmsManualScenario.setSteps(new HashSet<>());
    }

    createSteps(tmsManualScenario, testCaseManualScenarioRQ);
  }

  @Override
  @Transactional
  public void patchStep(TmsManualScenario tmsManualScenario,
      TmsTextManualScenarioRQ testCaseManualScenarioRQ) {
    if (testCaseManualScenarioRQ == null) {
      return;
    }

    var existingStep = tmsManualScenario
        .getSteps()
        .stream()
        .findFirst()
        .orElse(null);

    if (existingStep != null) {
      tmsStepMapper.patch(existingStep, tmsStepMapper.convertToTmsStep(testCaseManualScenarioRQ));
      tmsStepRepository.save(existingStep);
    } else {
      createStep(tmsManualScenario, testCaseManualScenarioRQ);
    }
  }

  @Override
  @Transactional
  public void patchSteps(TmsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    if (testCaseManualScenarioRQ == null || CollectionUtils.isEmpty(testCaseManualScenarioRQ.getSteps())) {
      return;
    }

    var steps = tmsStepMapper.convertToTmsSteps(testCaseManualScenarioRQ);
    tmsManualScenario.getSteps().addAll(steps);
    steps.forEach(step -> step.setManualScenario(tmsManualScenario));
    tmsStepRepository.saveAll(steps);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsStepRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (testCaseIds != null && !testCaseIds.isEmpty()) {
      tmsStepRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsStepRepository.deleteStepsByTestFolderId(projectId, folderId);
  }
}
