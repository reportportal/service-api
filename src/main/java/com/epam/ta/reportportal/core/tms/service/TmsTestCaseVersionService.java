package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.db.entity.TmsTestCase;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseDefaultVersionRQ;
import java.util.List;

public interface TmsTestCaseVersionService {

  void createDefaultTestCaseVersion(TmsTestCase tmsTestCase, TmsTestCaseDefaultVersionRQ testCaseVersion);

  void updateDefaultTestCaseVersion(TmsTestCase tmsTestCase, TmsTestCaseDefaultVersionRQ testCaseVersion);

  void patchDefaultTestCaseVersion(TmsTestCase tmsTestCase, TmsTestCaseDefaultVersionRQ testCaseVersion);

  void deleteAllByTestCaseId(Long testCaseId);

  void deleteAllByTestCaseIds(List<Long> testCaseIds);

  void deleteAllByTestFolderId(Long projectId, Long folderId);
}
