package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsStep;
import com.epam.ta.reportportal.entity.tms.TmsStepsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import java.util.Collection;
import java.util.List;

public interface TmsStepService {
  void createSteps(TmsStepsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void updateSteps(TmsStepsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void patchSteps(TmsStepsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  /**
   * Duplicates steps from original scenario to new scenario.
   *
   * @param originalSteps The steps from original scenario to duplicate.
   * @param newStepsScenario The new steps scenario to attach duplicated steps to.
   */
  void duplicateSteps(Collection<TmsStep> originalSteps, TmsStepsManualScenario newStepsScenario);
}
