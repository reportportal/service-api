package com.epam.ta.reportportal.core.tms.service;

import static com.epam.reportportal.rules.exception.ErrorType.NOT_FOUND;

import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.repository.TmsStepsManualScenarioRepository;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.mapper.TmsStepsManualScenarioMapper;
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
      TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var tmsStepsManualScenario = tmsStepsManualScenarioMapper.createTmsStepsManualScenario();

    tmsStepService.createSteps(tmsStepsManualScenario,
        (TmsStepsManualScenarioRQ) testCaseManualScenarioRQ);

    tmsManualScenario.setStepsScenario(tmsStepsManualScenario);
    tmsStepsManualScenario.setManualScenario(tmsManualScenario);

    tmsStepsManualScenarioRepository.save(tmsStepsManualScenario);
  }

  @Override
  @Transactional
  public void updateTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
    var stepsManualScenario = manualScenario.getStepsScenario();

    if (Objects.nonNull(stepsManualScenario)) {
      tmsStepService.updateSteps(stepsManualScenario,
          (TmsStepsManualScenarioRQ) testCaseManualScenarioRQ);
    } else {
      stepsManualScenario = tmsStepsManualScenarioMapper.createTmsStepsManualScenario();
      tmsStepService.createSteps(
          stepsManualScenario, (TmsStepsManualScenarioRQ) testCaseManualScenarioRQ
      );
      manualScenario.setStepsScenario(stepsManualScenario);
      stepsManualScenario.setManualScenario(manualScenario);
    }

    tmsStepsManualScenarioRepository.save(stepsManualScenario);
  }

  @Override
  @Transactional
  public void patchTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ) {
      var existingStepsManualScenario = manualScenario.getStepsScenario();
      if (Objects.nonNull(existingStepsManualScenario)) {

        tmsStepService.patchSteps(existingStepsManualScenario,
            (TmsStepsManualScenarioRQ) testCaseManualScenarioRQ);

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
}
