/*
 * Copyright 2021 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.impl.preparer;

import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.epam.reportportal.model.analyzer.IndexLog;
import com.epam.reportportal.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

/**
 * Standard strategy for preparing test items for analysis. Uses the original logic for test item preparation.
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service("standardTestItemPreparerService")
public class StandardTestItemPreparerService implements TestItemPreparationStrategy {

  private final TestItemRepository testItemRepository;
  private final LogService logService;

  public StandardTestItemPreparerService(TestItemRepository testItemRepository,
      LogService logService) {
    this.testItemRepository = testItemRepository;
    this.logService = logService;
  }

  @Override
  public List<IndexTestItem> prepare(Long launchId, Collection<TestItem> testItems) {
    final List<IndexTestItem> itemsForIndexing = testItems.stream()
        .filter(ITEM_CAN_BE_INDEXED)
        .map(AnalyzerUtils::fromTestItem)
        .collect(toList());
    return prepareWithLogs(launchId, itemsForIndexing);
  }

  /**
   * Prepares test items by loading from repository when no items provided.
   *
   * @param launchId the launch ID
   * @return prepared list of {@link IndexTestItem} for indexing
   */
  public List<IndexTestItem> prepareFromRepository(Long launchId) {
    final List<IndexTestItem> indexTestItems = testItemRepository.findIndexTestItemByLaunchId(
        launchId,
        List.of(JTestItemTypeEnum.STEP, JTestItemTypeEnum.BEFORE_METHOD,
            JTestItemTypeEnum.AFTER_METHOD)
    );
    return prepareWithLogs(launchId, indexTestItems);
  }

  private List<IndexTestItem> prepareWithLogs(Long launchId, List<IndexTestItem> indexTestItemList) {
    final Map<Long, List<IndexLog>> logsMapping = getLogsMapping(launchId,
        indexTestItemList.stream().map(IndexTestItem::getTestItemId).collect(toList())
    );

    return indexTestItemList.stream()
        .peek(indexTestItem -> ofNullable(logsMapping.get(indexTestItem.getTestItemId())).filter(
                CollectionUtils::isNotEmpty)
            .map(HashSet::new)
            .ifPresent(indexTestItem::setLogs))
        .filter(it -> CollectionUtils.isNotEmpty(it.getLogs()))
        .collect(toList());
  }

  private Map<Long, List<IndexLog>> getLogsMapping(Long launchId, List<Long> itemIds) {
    return logService.findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId,
        itemIds, LogLevel.ERROR.toInt());
  }
}
