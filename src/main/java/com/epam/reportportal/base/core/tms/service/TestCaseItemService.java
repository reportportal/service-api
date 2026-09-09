package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import java.util.Map;

public interface TestCaseItemService {

  TestItem createTestCaseItem(
      TmsTestCaseRS testCase,
      TestItem suiteItem,
      Launch launch);

  default TestItem createTestCaseItem(
      TmsTestCaseRS testCase,
      TestItem suiteItem,
      Launch launch,
      Map<Long, String> itemNamesCache) {
    return createTestCaseItem(testCase, suiteItem, launch);
  }

}
