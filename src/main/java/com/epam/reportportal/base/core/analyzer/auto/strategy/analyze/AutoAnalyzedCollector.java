/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.analyzer.auto.strategy.analyze;

import com.epam.reportportal.base.core.analyzer.auto.LogIndexer;
import com.epam.reportportal.base.core.item.UpdateTestItemHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Collects items previously processed by the auto-analyzer.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class AutoAnalyzedCollector implements AnalyzeItemsCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutoAnalyzedCollector.class);

  private final TestItemRepository testItemRepository;

  private final LogIndexer logIndexer;

  private final UpdateTestItemHandler updateTestItemHandler;

  @Autowired
  public AutoAnalyzedCollector(TestItemRepository testItemRepository, LogIndexer logIndexer,
      UpdateTestItemHandler updateTestItemHandler) {
    this.testItemRepository = testItemRepository;
    this.logIndexer = logIndexer;
    this.updateTestItemHandler = updateTestItemHandler;
  }

  @Override
  public List<Long> collectItems(Long projectId, Long launchId, Long userId, String userLogin) {
    List<Long> itemIds = testItemRepository.selectIdsByAnalyzedWithLevelGteExcludingIssueTypes(true,
        false,
        launchId,
        LogLevel.ERROR.toInt(),
        Collections.emptyList()
    );
    int deletedLogsCount = logIndexer.indexItemsRemove(projectId, itemIds);
    LOGGER.debug("{} logs deleted from analyzer", deletedLogsCount);
    updateTestItemHandler.resetItemsIssue(itemIds, projectId, userId, userLogin);
    return itemIds;
  }
}
