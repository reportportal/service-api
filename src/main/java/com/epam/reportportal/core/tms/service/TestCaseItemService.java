package com.epam.reportportal.core.tms.service;

import com.epam.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;

public interface TestCaseItemService {

  TestItem createTestCaseItem(
      TmsTestCaseRS testCase,
      TestItem suiteItem,
      Launch launch);

  void markAsHavingNestedChildren(TestItem testItem);
}
