package com.epam.reportportal.base.core.tms.service;

import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.model.Page;
import org.springframework.data.domain.Pageable;

public interface TestFolderItemService {

  TestItem findTestFolderItem(Long projectId, Long testFolderId, Launch launch);

  TestItem createTestFolderSuiteItem(Long projectId, Long testFolderId,
      Launch launch);

  void markAsHavingChildren(TestItem testFolderItem);

  void deleteTestFolderTestItemByTestItemId(Long testItemId);

  Page<TmsTestFolderRS> getSuiteFoldersByLaunch(Long projectId, Long launchId,
      Filter filter, Pageable pageable);
}