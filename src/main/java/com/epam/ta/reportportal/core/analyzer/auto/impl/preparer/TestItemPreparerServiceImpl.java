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

import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.jooq.enums.JTestItemTypeEnum;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLog;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class TestItemPreparerServiceImpl implements TestItemPreparerService {

	private final TestItemRepository testItemRepository;
	private final LogRepository logRepository;

	public TestItemPreparerServiceImpl(TestItemRepository testItemRepository, LogRepository logRepository) {
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
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
		final List<IndexTestItem> indexTestItems = testItemRepository.findIndexTestItemByLaunchId(launchId,
				List.of(JTestItemTypeEnum.STEP)
		);
		return prepare(launchId, indexTestItems);
	}

	private List<IndexTestItem> prepare(Long launchId, List<IndexTestItem> indexTestItemList) {
		final Map<Long, List<IndexLog>> logsMapping = getLogsMapping(launchId,
				indexTestItemList.stream().map(IndexTestItem::getTestItemId).collect(toList())
		);

		return indexTestItemList.stream()
				.peek(indexTestItem -> ofNullable(logsMapping.get(indexTestItem.getTestItemId())).filter(CollectionUtils::isNotEmpty)
						.map(HashSet::new)
						.ifPresent(indexTestItem::setLogs))
				.filter(it -> CollectionUtils.isNotEmpty(it.getLogs()))
				.collect(toList());
	}

	private Map<Long, List<IndexLog>> getLogsMapping(Long launchId, List<Long> itemIds) {
		return logRepository.findAllIndexUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemIds, LogLevel.ERROR.toInt());
	}

}
