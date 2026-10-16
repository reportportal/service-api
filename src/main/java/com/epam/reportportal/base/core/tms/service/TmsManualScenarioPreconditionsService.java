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
   * Creates preconditions for multiple manual scenarios in bulk: a single saveAll() across all
   * scenarios instead of one round trip per scenario.
   *
   * @param projectId               project id (for attachment resolution)
   * @param manualScenarios         manual scenarios, aligned by index with preconditionsPerScenario
   * @param preconditionsPerScenario preconditions for each scenario (may contain null entries)
   */
  void createPreconditionsBatch(Long projectId, List<TmsManualScenario> manualScenarios,
      List<TmsManualScenarioPreconditionsRQ> preconditionsPerScenario);

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
