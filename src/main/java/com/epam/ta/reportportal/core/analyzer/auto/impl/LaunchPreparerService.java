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

import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
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

	private final LogRepository logRepository;

	@Autowired
	public LaunchPreparerService(LogRepository logRepository) {
		this.logRepository = logRepository;
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

		Map<Long, List<Log>> logsMapping = logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId,
				itemsForIndexing.stream().map(TestItem::getItemId).collect(toList()),
				LogLevel.ERROR.toInt()
		).stream().collect(groupingBy(l -> l.getTestItem().getItemId()));

		return itemsForIndexing.stream()
				.map(it -> AnalyzerUtils.fromTestItem(it, ofNullable(logsMapping.get(it.getItemId())).orElseGet(Collections::emptyList)))
				.filter(it -> !CollectionUtils.isEmpty(it.getLogs()))
				.collect(toList());

	}
}
