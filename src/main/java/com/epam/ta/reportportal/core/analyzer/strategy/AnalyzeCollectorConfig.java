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
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Configuration
public class AnalyzeCollectorConfig {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LogIndexer logIndexer;

	@Autowired
	private IssueTypeHandler issueTypeHandler;

	private AnalyzeItemsCollector TO_INVESTIGATE_COLLECTOR = (project, launchId, username) -> {
		return testItemRepository.selectItemsInIssueByLaunch(launchId, TestItemIssueGroup.TO_INVESTIGATE.getLocator())
				.stream()
				.filter(it -> !it.getItemResults().getIssue().getIgnoreAnalyzer())
				.map(TestItem::getItemId)
				.collect(toList());
	};

	private AnalyzeItemsCollector AUTO_ANALYZED_COLLECTOR = (project, launchId, username) -> {
		List<TestItem> items = testItemRepository.selectByAutoAnalyzedStatus(true, launchId);
		List<Long> itemIds = items.stream().map(TestItem::getItemId).collect(Collectors.toList());
		logIndexer.cleanIndex(project, itemIds);
		resetItems(items, project, username);
		return itemIds;
	};

	private AnalyzeItemsCollector MANUALLY_ANALYZED_COLLECTOR = (project, launchId, username) -> {
		List<TestItem> items = testItemRepository.selectByAutoAnalyzedStatus(false, launchId);
		List<Long> itemIds = items.stream().map(TestItem::getItemId).collect(Collectors.toList());
		logIndexer.cleanIndex(project, itemIds);
		resetItems(items, project, username);
		return itemIds;
	};

	private void resetItems(List<TestItem> items, Long projectId, String username) {
		items.forEach(item -> {
			IssueType issueType = issueTypeHandler.defineIssueType(item.getItemId(),
					projectId,
					TestItemIssueGroup.TO_INVESTIGATE.getLocator()
			);
			IssueEntity issueEntity = new IssueEntityBuilder(item.getItemResults().getIssue()).addIssueType(issueType)
					.addIgnoreFlag(item.getItemResults().getIssue().getIgnoreAnalyzer())
					.addAutoAnalyzedFlag(true)
					.get();
			issueEntity.setIssueId(item.getItemId());
			item.getItemResults().setIssue(issueEntity);
			testItemRepository.save(item);
		});
	}

	@Bean(name = "analyzerModeMapping")
	public Map<AnalyzeItemsMode, AnalyzeItemsCollector> getAnalyzerModeMapping() {
		Map<AnalyzeItemsMode, AnalyzeItemsCollector> mapping = new HashMap<>();
		mapping.put(AnalyzeItemsMode.TO_INVESTIGATE, TO_INVESTIGATE_COLLECTOR);
		mapping.put(AnalyzeItemsMode.AUTO_ANALYZED, AUTO_ANALYZED_COLLECTOR);
		mapping.put(AnalyzeItemsMode.MANUALLY_ANALYZED, MANUALLY_ANALYZED_COLLECTOR);
		return mapping;
	}

	@Bean
	public AnalyzeCollectorFactory analyzeCollectorFactory() {
		return new AnalyzeCollectorFactory(getAnalyzerModeMapping());
	}
}
