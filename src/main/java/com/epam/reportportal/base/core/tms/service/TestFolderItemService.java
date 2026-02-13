package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;

public interface TestFolderItemService {

  TestItem findTestFolderItem(Long projectId, Long testFolderId, Launch launch);

  TestItem createTestFolderSuiteItem(Long projectId, Long testFolderId,
      Launch launch);

  void markAsHavingChildren(TestItem testFolderItem);

  void deleteTestFolderTestItemByTestItemId(Long testItemId);
}
