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

import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TestItemPreparerServiceImpl implements TestItemPreparerService {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

  private final TestItemRepository testItemRepository;
  private final LogService logService;

  public TestItemPreparerServiceImpl(TestItemRepository testItemRepository, LogService logService,
      LogRepository logRepository) {
    this.testItemRepository = testItemRepository;
    this.logService = logService;
  }

  @Override
  public List<IndexTestItem> prepare(Long launchId, Collection<TestItem> testItems) {
    final List<IndexTestItem> itemsForIndexing = testItems.stream()
        .filter(ITEM_CAN_BE_INDEXED)
        .map(AnalyzerUtils::fromTestItem)
        .collect(toList());
    return prepare(launchId, itemsForIndexing);
  }

  @Override
  public List<IndexTestItem> prepare(Long launchId) {
    final List<IndexTestItem> indexTestItems = testItemRepository.findIndexTestItemByLaunchId(
        launchId,
        List.of(JTestItemTypeEnum.STEP, JTestItemTypeEnum.BEFORE_METHOD,
            JTestItemTypeEnum.AFTER_METHOD)
    );
    return prepare(launchId, indexTestItems);
  }

  private List<IndexTestItem> prepare(Long launchId, List<IndexTestItem> indexTestItemList) {
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
    if (itemIds.size() == 1) {
      LOGGER.info("Prepare single index");
      Map<Long, List<IndexLog>> result = new HashMap<>();
      List<IndexLog> indexlogs = new ArrayList<>();

        IndexLog indexLog = new IndexLog();
        indexLog.setLogId(1L);
        indexLog.setLogLevel(2);
        indexLog.setMessage("ERROR");
        indexLog.setLogTime(LocalDateTime.now());
        indexLog.setClusterId(1L);
        indexlogs.add(indexLog);

      result.put(itemIds.get(0), indexlogs);
      return result;
    }

    return logService.findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId,
        itemIds, LogLevel.ERROR.toInt());
  }

}
