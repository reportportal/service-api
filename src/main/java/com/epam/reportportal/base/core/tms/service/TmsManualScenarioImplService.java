package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import java.util.List;

public interface TmsManualScenarioImplService {

  TmsManualScenarioType getTmsManualScenarioType();

  /**
   * Creates implementation-specific manual scenario data.
   */
  void createTmsManualScenarioImpl(Long projectId, TmsManualScenario tmsManualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRq);

  /**
   * Updates implementation-specific manual scenario data.
   */
  void updateTmsManualScenarioImpl(Long projectId, TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRq);

  /**
   * Partially updates implementation-specific manual scenario data.
   */
  void patchTmsManualScenarioImpl(Long projectId, TmsManualScenario manualScenario,
      TmsManualScenarioRQ testCaseManualScenarioRq);

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
