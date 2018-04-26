/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.core.configs;

import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeCollectorFactory;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsCollector;
import com.epam.ta.reportportal.core.analyzer.strategy.AnalyzeItemsMode;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Configuration
public class AnalyzeCollectorConfig {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Autowired
	private ILogIndexer logIndexer;

	private AnalyzeItemsCollector TO_INVESTIGATE_COLLECTOR = (project, username, launchId) -> testItemRepository.findInIssueTypeItems(
			TestItemIssueType.TO_INVESTIGATE.getLocator(), launchId)
			.stream()
			.filter(it -> !it.getIssue().isIgnoreAnalyzer())
			.collect(toList());

	private AnalyzeItemsCollector AUTO_ANALYZED_COLLECTOR = (project, username, launchId) -> {
		List<TestItem> itemsByAutoAnalyzedStatus = testItemRepository.findItemsByAutoAnalyzedStatus(true, launchId);
		itemsByAutoAnalyzedStatus = resetItems(itemsByAutoAnalyzedStatus, project.getName(), username);
		return itemsByAutoAnalyzedStatus;
	};

	private AnalyzeItemsCollector MANUALLY_ANALYZED_COLLECTOR = (project, username, launchId) -> {
		List<TestItem> itemsByManuallyAnalyzedStatus = testItemRepository.findItemsByAutoAnalyzedStatus(false, launchId);
		itemsByManuallyAnalyzedStatus = resetItems(itemsByManuallyAnalyzedStatus, project.getName(), username);
		return itemsByManuallyAnalyzedStatus;
	};

	private List<TestItem> resetItems(List<TestItem> items, String projectName, String username) {
		logIndexer.cleanIndex(projectName, items.stream().map(TestItem::getId).collect(toList()));
		Map<IssueDefinition, TestItem> definitions = new HashMap<>();
		items.forEach(it -> {
			IssueDefinition issueDefinition = new IssueDefinition();
			issueDefinition.setIssue(IssueConverter.TO_MODEL.apply(new TestItemIssue()));
			issueDefinition.setId(it.getId());
			definitions.put(issueDefinition, SerializationUtils.clone(it));
			it.setIssue(new TestItemIssue());
		});
		testItemRepository.save(items);
		eventPublisher.publishEvent(new ItemIssueTypeDefined(definitions, username, projectName));
		return items;
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
