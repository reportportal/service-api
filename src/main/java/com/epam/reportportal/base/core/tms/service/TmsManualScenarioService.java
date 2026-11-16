package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsManualScenario;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import java.util.List;

public interface TmsManualScenarioService {

  TmsManualScenario createTmsManualScenario(long projectId, TmsTestCaseVersion testCaseVersion,
      TmsManualScenarioRQ testCaseManualScenarioRQ);

  /**
   * Creates manual scenarios (and their preconditions/requirements/type-specific data) for
   * multiple test case versions in bulk, batching DB writes across all of them instead of doing
   * one round trip per version. Used by high-volume flows such as CSV import.
   *
   * @param projectId                   project id
   * @param testCaseVersions            test case versions, aligned by index with
   *                                    testCaseManualScenarioRQs
   * @param testCaseManualScenarioRQs   request DTOs for each version; an entry may be null if that
   *                                    version has no manual scenario
   * @return created scenarios aligned by index with the input lists (null where the input RQ was
   *     null)
   */
  List<TmsManualScenario> createTmsManualScenariosBatch(long projectId,
      List<TmsTestCaseVersion> testCaseVersions,
      List<TmsManualScenarioRQ> testCaseManualScenarioRQs);

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
