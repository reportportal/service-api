package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import java.util.List;

public interface TmsManualScenarioImplService {

  TmsManualScenarioType getTmsManualScenarioType();

  void createTmsManualScenarioImpl(TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void updateTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void patchTmsManualScenarioImpl(TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  /**
   * Duplicates the implementation-specific part of a manual scenario.
   *
   * @param newScenario      The new scenario entity.
   * @param originalScenario The original scenario to duplicate from.
   */
  void duplicateManualScenarioImpl(TmsManualScenario newScenario,
      TmsManualScenario originalScenario);
}
