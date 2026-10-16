package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import java.util.List;

/**
 * Service interface for managing TMS manual scenario requirements.
 */
public interface TmsManualScenarioRequirementService {

  void createRequirements(TmsManualScenario tmsManualScenario,
      List<TmsRequirementRQ> requirements);

  /**
   * Creates requirements for multiple manual scenarios in bulk: a single existence check and a
   * single saveAll() across all scenarios, instead of one round trip per scenario.
   *
   * @param manualScenarios        manual scenarios, aligned by index with requirementsPerScenario
   * @param requirementsPerScenario requirements for each scenario (may contain null/empty entries)
   */
  void createRequirementsBatch(List<TmsManualScenario> manualScenarios,
      List<List<TmsRequirementRQ>> requirementsPerScenario);

  void updateRequirements(TmsManualScenario manualScenario,
      List<TmsRequirementRQ> requirements);

  void patchRequirements(TmsManualScenario existingManualScenario,
      List<TmsRequirementRQ> requirements);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  void duplicateRequirements(TmsManualScenario originalScenario,
      TmsManualScenario duplicatedScenario);
}
