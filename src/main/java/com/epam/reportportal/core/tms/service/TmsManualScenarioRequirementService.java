package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsRequirementRQ;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsManualScenario;
import java.util.List;

/**
 * Service interface for managing TMS manual scenario requirements.
 */
public interface TmsManualScenarioRequirementService {

  void createRequirements(TmsManualScenario tmsManualScenario,
      List<TmsRequirementRQ> requirements);

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
