package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCaseVersion;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioRQ;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TmsTestCaseVersionService {

  TmsTestCaseVersion createDefaultTestCaseVersion(TmsTestCase tmsTestCase, @Valid TmsManualScenarioRQ manualScenarioRQ);

  TmsTestCaseVersion updateDefaultTestCaseVersion(TmsTestCase tmsTestCase, @Valid TmsManualScenarioRQ manualScenarioRQ);

  TmsTestCaseVersion patchDefaultTestCaseVersion(TmsTestCase tmsTestCase, @Valid TmsManualScenarioRQ manualScenarioRQ);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);

  TmsTestCaseVersion getDefaultVersion(Long testCaseId);

  Map<Long, TmsTestCaseVersion> getDefaultVersions(List<Long> testCaseIds);
}
