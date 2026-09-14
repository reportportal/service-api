package com.epam.reportportal.base.core.tms.service;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.mapper.TmsStepsManualScenarioMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsStepsManualScenarioRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TmsStepsManualScenarioImplService implements TmsManualScenarioImplService {

  private static final String STEPS_MANUAL_SCENARIO_FOR_MANUAL_SCENARIO =
      "Steps Manual Scenario for Manual Scenario with id: %d";

  private final TmsStepService tmsStepService;
  private final TmsStepsManualScenarioMapper tmsStepsManualScenarioMapper;
  private final TmsStepsManualScenarioRepository tmsStepsManualScenarioRepository;

  @Override
  public TmsManualScenarioType getTmsManualScenarioType() {
    return TmsManualScenarioType.STEPS;
  }

  @Override
  @Transactional
  public void createTmsManualScenarioImpl(
      Long projectId,
      TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRq) {
    var tmsStepsManualScenario = tmsStepsManualScenarioMapper.createTmsStepsManualScenario();

    tmsStepsManualScenario.setManualScenario(tmsManualScenario);

    tmsStepsManualScenario = tmsStepsManualScenarioRepository.save(tmsStepsManualScenario);

    tmsManualScenario.setStepsScenario(tmsStepsManualScenario);

    tmsStepService.createSteps(projectId, tmsStepsManualScenario,
        (TmsStepsManualScenarioRQ) testCaseManualScenarioRq);
  }

  @Override
  @Transactional
  public void updateTmsManualScenarioImpl(Long projectId, TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRq) {
    var stepsManualScenario = manualScenario.getStepsScenario();

    if (Objects.nonNull(stepsManualScenario)) {
      tmsStepService.updateSteps(projectId, stepsManualScenario,
          (TmsStepsManualScenarioRQ) testCaseManualScenarioRq);
    } else {
      stepsManualScenario = tmsStepsManualScenarioMapper.createTmsStepsManualScenario();
      tmsStepService.createSteps(projectId, stepsManualScenario, (TmsStepsManualScenarioRQ) testCaseManualScenarioRq);
      manualScenario.setStepsScenario(stepsManualScenario);
      stepsManualScenario.setManualScenario(manualScenario);
    }

    tmsStepsManualScenarioRepository.save(stepsManualScenario);
  }

  @Override
  @Transactional
  public void patchTmsManualScenarioImpl(Long projectId, TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRq) {
    var existingStepsManualScenario = manualScenario.getStepsScenario();
    if (Objects.nonNull(existingStepsManualScenario)) {

      tmsStepService.patchSteps(projectId, existingStepsManualScenario,
          (TmsStepsManualScenarioRQ) testCaseManualScenarioRq);

      tmsStepsManualScenarioRepository.save(existingStepsManualScenario);
    } else {
      throw new ReportPortalException(
          NOT_FOUND, STEPS_MANUAL_SCENARIO_FOR_MANUAL_SCENARIO.formatted(manualScenario.getId()));
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseId(Long testCaseId) {
    tmsStepService.deleteAllByTestCaseId(testCaseId);
    tmsStepsManualScenarioRepository.deleteAllByTestCaseId(testCaseId);
  }

  @Override
  @Transactional
  public void deleteAllByTestCaseIds(List<Long> testCaseIds) {
    if (CollectionUtils.isNotEmpty(testCaseIds)) {
      tmsStepService.deleteAllByTestCaseIds(testCaseIds);
      tmsStepsManualScenarioRepository.deleteAllByTestCaseIds(testCaseIds);
    }
  }

  @Override
  @Transactional
  public void deleteAllByTestFolderId(Long projectId, Long folderId) {
    tmsStepService.deleteAllByTestFolderId(projectId, folderId);
    tmsStepsManualScenarioRepository.deleteAllByTestFolderId(projectId, folderId);
  }

  @Override
  @Transactional
  public void duplicateManualScenarioImpl(TmsManualScenario newScenario, TmsManualScenario originalScenario) {
    var originalStepsScenario = originalScenario.getStepsScenario();
    if (Objects.nonNull(originalStepsScenario)) {
      var duplicatedStepsScenario = tmsStepsManualScenarioMapper.createTmsStepsManualScenario(newScenario);
      newScenario.setStepsScenario(duplicatedStepsScenario);
      tmsStepsManualScenarioRepository.save(duplicatedStepsScenario);

      tmsStepService.duplicateSteps(originalStepsScenario.getSteps(), duplicatedStepsScenario);
    }
  }
}
