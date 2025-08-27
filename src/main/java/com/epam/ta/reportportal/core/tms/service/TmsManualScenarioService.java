package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsManualScenario;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import java.util.List;

public interface TmsManualScenarioService {

  TmsManualScenario createTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  TmsManualScenario updateTmsManualScenario(TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  TmsManualScenario patchTmsManualScenario(TmsTestCaseVersion testCaseVersion,
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
