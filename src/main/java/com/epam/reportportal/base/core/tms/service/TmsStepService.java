package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStep;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsStepsManualScenario;
import java.util.Collection;
import java.util.List;

public interface TmsStepService {

  /**
   * Creates steps for steps-based manual scenario.
   */
  void createSteps(Long projectId, TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRq);

  /**
   * Updates steps for steps-based manual scenario.
   */
  void updateSteps(Long projectId, TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRq);

  /**
   * Partially updates steps for steps-based manual scenario.
   */
  void patchSteps(Long projectId, TmsStepsManualScenario tmsManualScenario,
      TmsStepsManualScenarioRQ testCaseManualScenarioRq);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  /**
   * Duplicates steps from original scenario to new scenario.
   *
   * @param originalSteps    The steps from original scenario to duplicate.
   * @param newStepsScenario The new steps scenario to attach duplicated steps to.
   */
  void duplicateSteps(Collection<TmsStep> originalSteps, TmsStepsManualScenario newStepsScenario);
}
