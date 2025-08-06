package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsStepsManualScenario;
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
  public void createSteps(TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    if (CollectionUtils.isEmpty(testCaseManualScenarioRQ.getSteps())) {
      return;
    }

    var steps = tmsStepMapper.convertToTmsSteps(testCaseManualScenarioRQ);

    tmsManualScenario.setSteps(steps);
    steps.forEach(step -> step.setStepsManualScenario(tmsManualScenario));

    tmsStepRepository.saveAll(steps);
  }

  @Override
  @Transactional
  public void updateSteps(TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    if (CollectionUtils.isNotEmpty(tmsManualScenario.getSteps())) {
      tmsStepRepository.deleteAll(tmsManualScenario.getSteps());
      tmsManualScenario.setSteps(new HashSet<>());
    }

    createSteps(tmsManualScenario, testCaseManualScenarioRQ);
  }

  @Override
  @Transactional
  public void patchSteps(TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRQ) {
    if (testCaseManualScenarioRQ == null || CollectionUtils.isEmpty(testCaseManualScenarioRQ.getSteps())) {
      return;
    }

    var steps = tmsStepMapper.convertToTmsSteps(testCaseManualScenarioRQ);
    tmsManualScenario.getSteps().addAll(steps);
    steps.forEach(step -> step.setStepsManualScenario(tmsManualScenario));
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
