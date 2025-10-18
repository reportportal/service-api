package com.epam.ta.reportportal.core.tms.service;

import com.epam.ta.reportportal.entity.tms.TmsTestCaseExecution;
import java.util.List;
import java.util.Map;

public interface TmsTestCaseExecutionService {

  Map<Long, TmsTestCaseExecution> getLastTestCasesExecutionsByTestCaseIds(List<Long> testCaseIds);

  TmsTestCaseExecution getLastTestCaseExecution(Long testCaseId);
}
