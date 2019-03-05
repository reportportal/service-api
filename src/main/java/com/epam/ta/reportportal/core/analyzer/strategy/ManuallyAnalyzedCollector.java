/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.analyzer.strategy;

import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.item.UpdateTestItemHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ManuallyAnalyzedCollector implements AnalyzeItemsCollector {

	private final TestItemRepository testItemRepository;

	private final LogIndexer logIndexer;

	private final UpdateTestItemHandler updateTestItemHandler;

	@Autowired
	public ManuallyAnalyzedCollector(TestItemRepository testItemRepository, LogIndexer logIndexer,
			UpdateTestItemHandler updateTestItemHandler) {
		this.testItemRepository = testItemRepository;
		this.logIndexer = logIndexer;
		this.updateTestItemHandler = updateTestItemHandler;
	}

	@Override
	public List<Long> collectItems(Long projectId, Long launchId, String login) {
		List<TestItem> items = testItemRepository.selectByAutoAnalyzedStatus(false, launchId);
		List<Long> itemIds = items.stream().map(TestItem::getItemId).collect(Collectors.toList());
		logIndexer.cleanIndex(projectId, itemIds);
		updateTestItemHandler.resetItemsIssue(items, projectId);
		return itemIds;
	}

}
