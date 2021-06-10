/*
 * Copyright 2019 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.auto.impl;

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.util.Predicates.ITEM_CAN_BE_INDEXED;
import static com.epam.ta.reportportal.util.Predicates.LAUNCH_CAN_BE_INDEXED;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class LaunchPreparerService {

	private final LaunchRepository launchRepository;

	private final LogRepository logRepository;

	@Autowired
	public LaunchPreparerService(LogRepository logRepository, LaunchRepository launchRepository) {
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
	}

	public Optional<IndexLaunch> prepare(Launch launch, List<TestItem> testItems, AnalyzerConfig analyzerConfig) {
		if (LAUNCH_CAN_BE_INDEXED.test(launch)) {
			List<IndexTestItem> rqTestItems = prepareItemsForIndexing(launch.getId(), testItems);
			if (!CollectionUtils.isEmpty(rqTestItems)) {
				return Optional.of(createIndexLaunch(launch.getProjectId(), launch.getId(), launch.getName(), analyzerConfig, rqTestItems));
			}
		}
		return Optional.empty();
	}

	public List<IndexTestItem> prepare(Long launchId, List<IndexTestItem> indexTestItemList) {
		final Map<Long, List<Log>> logsMapping = getLogsMapping(launchId,
				indexTestItemList.stream().map(IndexTestItem::getTestItemId).collect(toList())
		);

		return indexTestItemList.stream()
				.peek(indexTestItem -> ofNullable(logsMapping.get(indexTestItem.getTestItemId())).filter(CollectionUtils::isNotEmpty)
						.map(AnalyzerUtils::fromLogs)
						.ifPresent(indexTestItem::setLogs))
				.filter(it -> CollectionUtils.isNotEmpty(it.getLogs()))
				.collect(toList());
	}

	public List<IndexLaunch> prepareLaunches(AnalyzerConfig analyzerConfig, List<TestItem> testItems) {
		return testItems.stream().collect(Collectors.groupingBy(TestItem::getLaunchId)).entrySet().stream().flatMap(entry -> {
			Launch launch = launchRepository.findById(entry.getKey())
					.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, entry.getKey()));
			return prepare(launch, entry.getValue(), analyzerConfig).stream();
		}).collect(Collectors.toList());
	}

	private IndexLaunch createIndexLaunch(Long projectId, Long launchId, String name, AnalyzerConfig analyzerConfig,
			List<IndexTestItem> rqTestItems) {
		IndexLaunch rqLaunch = new IndexLaunch();
		rqLaunch.setLaunchId(launchId);
		rqLaunch.setLaunchName(name);
		rqLaunch.setProjectId(projectId);
		rqLaunch.setAnalyzerConfig(analyzerConfig);
		rqLaunch.setTestItems(rqTestItems);
		return rqLaunch;
	}

	/**
	 * Creates {@link IndexTestItem} from suitable {@link TestItem}
	 * for indexing with logs greater than {@link LogLevel#ERROR}
	 *
	 * @param launchId  {@link TestItem#getLaunchId()}
	 * @param testItems Test item for preparing
	 * @return Prepared list of {@link IndexTestItem} for indexing
	 */
	private List<IndexTestItem> prepareItemsForIndexing(Long launchId, List<TestItem> testItems) {
		List<TestItem> itemsForIndexing = testItems.stream().filter(ITEM_CAN_BE_INDEXED).collect(toList());

		Map<Long, List<Log>> logsMapping = getLogsMapping(launchId, itemsForIndexing.stream().map(TestItem::getItemId).collect(toList()));

		return itemsForIndexing.stream()
				.map(it -> AnalyzerUtils.fromTestItem(it, ofNullable(logsMapping.get(it.getItemId())).orElseGet(Collections::emptyList)))
				.filter(it -> !CollectionUtils.isEmpty(it.getLogs()))
				.collect(toList());

	}

	private Map<Long, List<Log>> getLogsMapping(Long launchId, List<Long> itemIds) {
		return logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemIds, LogLevel.ERROR.toInt())
				.stream()
				.collect(groupingBy(l -> l.getTestItem().getItemId()));
	}
}
