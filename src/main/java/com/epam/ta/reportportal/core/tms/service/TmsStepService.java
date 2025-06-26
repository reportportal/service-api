package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import java.util.List;

public interface TmsStepService {

  void createStep(TmsManualScenario tmsManualScenario, TmsTextManualScenarioRQ testCaseManualScenarioRQ);

  void createSteps(TmsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void updateStep(TmsManualScenario tmsManualScenario, TmsTextManualScenarioRQ testCaseManualScenarioRQ);

  void updateSteps(TmsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void patchStep(TmsManualScenario tmsManualScenario, TmsTextManualScenarioRQ testCaseManualScenarioRQ);

  void patchSteps(TmsManualScenario tmsManualScenario, TmsStepsManualScenarioRQ testCaseManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);
}
