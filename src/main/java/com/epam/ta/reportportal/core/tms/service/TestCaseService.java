package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.core.tms.dto.TestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRS;
import java.util.List;

public interface TestCaseService {

  TestCaseRS createTestCase(TestCaseRQ inputDto);

  TestCaseRS updateTestCase(long testCaseId, TestCaseRQ inputDto);

  TestCaseRS getTestCaseById(long projectId, long id);

  List<TestCaseRS> getTestCaseByProjectId(long projectId);
}
