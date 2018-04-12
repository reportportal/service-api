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
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
@Configuration
public class AnalyzeStrategyConfig {

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ILogIndexer logIndexer;

	private AnalyzeItemsCollector TO_INVESTIGATE_COLLECTOR = (project, launchId) -> testItemRepository.findInIssueTypeItems(
			TestItemIssueType.TO_INVESTIGATE.getLocator(), launchId);

	private AnalyzeItemsCollector AUTO_ANALYZED_COLLECTOR = (project, launchId) -> {
		List<TestItem> itemsByAutoAnalyzedStatus = testItemRepository.findItemsByAutoAnalyzedStatus(true, launchId);
		logIndexer.cleanIndex(project, itemsByAutoAnalyzedStatus.stream().map(TestItem::getId).collect(Collectors.toList()));
		return itemsByAutoAnalyzedStatus;
	};

	private AnalyzeItemsCollector MANUALLY_ANALYZED_COLLECTOR = (project, launchId) -> {
		List<TestItem> itemsByAutoAnalyzedStatus = testItemRepository.findItemsByAutoAnalyzedStatus(false, launchId);
		logIndexer.cleanIndex(project, itemsByAutoAnalyzedStatus.stream().map(TestItem::getId).collect(Collectors.toList()));
		return itemsByAutoAnalyzedStatus;
	};

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
