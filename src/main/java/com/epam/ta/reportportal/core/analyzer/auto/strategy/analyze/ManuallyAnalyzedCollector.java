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

package com.epam.ta.reportportal.core.analyzer.auto.strategy.analyze;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ManuallyAnalyzedCollector implements AnalyzeItemsCollector {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeItemsCollector.class);

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final LogIndexer logIndexer;

	private final UpdateTestItemHandler updateTestItemHandler;

	@Autowired
	public ManuallyAnalyzedCollector(TestItemRepository testItemRepository, LogRepository logRepository, LogIndexer logIndexer,
			UpdateTestItemHandler updateTestItemHandler) {
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.logIndexer = logIndexer;
		this.updateTestItemHandler = updateTestItemHandler;
	}

	@Override
	public List<Long> collectItems(Long projectId, Long launchId, ReportPortalUser user) {
		List<Long> itemIds = testItemRepository.selectIdsByAnalyzedWithLevelGte(false, launchId, LogLevel.ERROR.toInt());
		Long deletedLogsCount = logIndexer.cleanIndex(
				projectId,
				logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(launchId, itemIds, LogLevel.ERROR.toInt())
		).join();
		LOGGER.debug("{} logs deleted from analyzer", deletedLogsCount);
		updateTestItemHandler.resetItemsIssue(itemIds, projectId, user);
		return itemIds;
	}

}
