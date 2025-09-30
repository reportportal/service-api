package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsTestCase;
import com.epam.ta.reportportal.entity.tms.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

public interface TmsTestCaseVersionService {

  TmsTestCaseVersion createDefaultTestCaseVersion(TmsTestCase tmsTestCase, @Valid TmsManualScenarioRQ manualScenarioRQ);

  TmsTestCaseVersion updateDefaultTestCaseVersion(TmsTestCase tmsTestCase, @Valid TmsManualScenarioRQ manualScenarioRQ);

  TmsTestCaseVersion patchDefaultTestCaseVersion(TmsTestCase tmsTestCase, @Valid TmsManualScenarioRQ manualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  TmsTestCaseVersion getDefaultVersion(Long testCaseId);

  Map<Long, TmsTestCaseVersion> getDefaultVersions(List<Long> testCaseIds);

  /**
   * Duplicates a default version for a new test case.
   *
   * @param newTestCase The new test case entity.
   * @param originalVersion The original version to duplicate.
   * @return The duplicated version.
   */
  TmsTestCaseVersion duplicateDefaultVersion(TmsTestCase newTestCase, TmsTestCaseVersion originalVersion);
}
