package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioPreconditionsRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import java.util.List;

public interface TmsManualScenarioPreconditionsService {

  /**
   * Creates preconditions for manual scenario.
   */
  void createPreconditions(Long projectId, TmsManualScenario tmsManualScenario,
      TmsManualScenarioPreconditionsRQ preconditions);

  /**
   * Updates preconditions for manual scenario.
   */
  void updatePreconditions(Long projectId, TmsManualScenario manualScenario,
      TmsManualScenarioPreconditionsRQ preconditions);

  /**
   * Partially updates preconditions for manual scenario.
   */
  void patchPreconditions(Long projectId, TmsManualScenario existingManualScenario,
      TmsManualScenarioPreconditionsRQ preconditions);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicatePreconditions(TmsManualScenario originalScenario,
      TmsManualScenario duplicatedScenario);
}
