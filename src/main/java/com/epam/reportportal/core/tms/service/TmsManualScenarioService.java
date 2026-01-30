package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.core.tms.dto.TmsManualScenarioRQ;
import java.util.List;

public interface TmsManualScenarioService {

  TmsManualScenario createTmsManualScenario(long projectId, TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  TmsManualScenario updateTmsManualScenario(long projectId, TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  TmsManualScenario patchTmsManualScenario(long projectId, TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  /**
   * Duplicates a manual scenario for a new version.
   *
   * @param newVersion       The new version entity.
   * @param originalScenario The original scenario to duplicate.
   * @return The duplicated scenario.
   */
  TmsManualScenario duplicateManualScenario(TmsTestCaseVersion newVersion,
      TmsManualScenario originalScenario);
}
