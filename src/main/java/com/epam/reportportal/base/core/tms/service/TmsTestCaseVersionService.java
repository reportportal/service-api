package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseVersion;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing TMS Test Case Versions.
 */
public interface TmsTestCaseVersionService {

  /**
   * Creates default test case version.
   *
   * @param projectId
   * @param tmsTestCase         test case entity
   * @param tmsManualScenarioRQ manual scenario request
   * @return created test case version
   */
  TmsTestCaseVersion createDefaultTestCaseVersion(long projectId, TmsTestCase tmsTestCase,
      @Valid TmsManualScenarioRQ tmsManualScenarioRQ);

  /**
   * Updates default test case version.
   *
   * @param projectId
   * @param tmsTestCase         test case entity
   * @param tmsManualScenarioRQ manual scenario request
   * @return updated test case version
   */
  TmsTestCaseVersion updateDefaultTestCaseVersion(long projectId, TmsTestCase tmsTestCase,
      @Valid TmsManualScenarioRQ tmsManualScenarioRQ);

  /**
   * Patches default test case version.
   *
   * @param projectId
   * @param tmsTestCase         test case entity
   * @param tmsManualScenarioRQ manual scenario request
   * @return patched test case version
   */
  TmsTestCaseVersion patchDefaultTestCaseVersion(long projectId, TmsTestCase tmsTestCase,
      @Valid TmsManualScenarioRQ tmsManualScenarioRQ);

  /**
   * Deletes all versions by test case ID.
   *
   * @param testCaseId test case ID
   */
  void deleteAllByTestCaseId(Long testCaseId);

  /**
   * Deletes all versions by test case IDs.
   *
   * @param testCaseIds list of test case IDs
   */
  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  /**
   * Deletes all versions by test folder ID.
   *
   * @param projectId project ID
   * @param folderId  folder ID
   */
  void deleteAllByTestFolderId(Long projectId, Long folderId);

  /**
   * Gets default version by test case ID.
   *
   * @param testCaseId test case ID
   * @return default test case version
   * @throws ReportPortalException if version not found
   */
  TmsTestCaseVersion getDefaultVersion(Long testCaseId);

  /**
   * Gets default versions for multiple test cases.
   *
   * @param testCaseIds list of test case IDs
   * @return map of test case ID to default version
   */
  Map<Long, TmsTestCaseVersion> getDefaultVersions(List<Long> testCaseIds);

  /**
   * Duplicates default version for new test case.
   *
   * @param newTestCase     new test case entity
   * @param originalVersion original version to duplicate
   * @return duplicated test case version
   */
  TmsTestCaseVersion duplicateDefaultVersion(TmsTestCase newTestCase,
      TmsTestCaseVersion originalVersion);

  /**
   * Finds default version by test case ID.
   *
   * @param testCaseId test case ID
   * @return optional default version
   */
  Optional<TmsTestCaseVersion> findDefaultByTestCaseId(Long testCaseId);

  /**
   * Gets default version ID by test case ID.
   *
   * @param testCaseId test case ID
   * @return optional default version ID
   */
  Optional<Long> findDefaultVersionIdByTestCaseId(Long testCaseId);
}
