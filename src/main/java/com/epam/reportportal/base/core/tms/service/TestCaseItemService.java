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

  /**
   * Same as {@link #createTestCaseItem(TmsTestCaseRS, TestItem, Launch)}, but reuses a
   * caller-provided cache of parent item names ({@code testItemNamesByIds}, keyed by item id) for
   * test case hash generation to avoid re-fetching names already resolved for other test cases in
   * the same batch.
   */
  default TestItem createTestCaseItem(
      TmsTestCaseRS testCase,
      TestItem suiteItem,
      Launch launch,
      Map<Long, String> testItemNamesByIds) {
    return createTestCaseItem(testCase, suiteItem, launch);
  }

}
