package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsStepsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import java.util.List;

public interface TmsStepService {
  void createSteps(TmsStepsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void updateSteps(TmsStepsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void patchSteps(TmsStepsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);
}
