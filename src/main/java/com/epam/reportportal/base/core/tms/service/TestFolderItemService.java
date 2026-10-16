package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.model.Page;
import java.util.Map;
import org.springframework.data.domain.Pageable;

public interface TestFolderItemService {

  TestItem findTestFolderItem(Long projectId, Long testFolderId, Launch launch);

  /**
   * Same as {@link #findTestFolderItem(Long, Long, Launch)}, but reuses a caller-provided cache of
   * already resolved/created SUITE items ({@code suiteItemsByIds}, keyed by test folder id) to
   * avoid repeated lookups for folders shared by multiple test cases in the same batch.
   */
  default TestItem findTestFolderItem(Long projectId, Long testFolderId, Launch launch,
      Map<Long, TestItem> suiteItemsByIds) {
    return findTestFolderItem(projectId, testFolderId, launch);
  }

  TestItem createTestFolderSuiteItem(Long projectId, Long testFolderId,
      Launch launch);

  void markAsHavingChildren(TestItem testFolderItem);

  void deleteTestFolderTestItemByTestItemId(Long testItemId);

  Page<TmsTestFolderRS> getSuiteFoldersByLaunch(Long projectId, Long launchId,
      Filter filter, Pageable pageable);
}
