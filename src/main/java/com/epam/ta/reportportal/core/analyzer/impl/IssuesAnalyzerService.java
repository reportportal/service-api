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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.IAnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Default implementation of {@link IIssuesAnalyzer}.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service
public class IssuesAnalyzerService implements IIssuesAnalyzer {

	@Autowired
	private IAnalyzerServiceClient analyzerServiceClient;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private ILogIndexer logIndexer;

	@Override
	public boolean hasAnalyzers() {
		return analyzerServiceClient.hasClients();
	}

	@Override
	public void analyze(Launch launch, List<TestItem> testItems) {
		if (launch != null) {
			List<IndexTestItem> rqTestItems = prepareItems(testItems);
			Set<AnalyzedItemRs> rs = analyze(rqTestItems, launch);
			if (!isEmpty(rs)) {
				List<TestItem> updatedItems = updateTestItems(rs, testItems);
				saveUpdatedItems(updatedItems, launch);
				logIndexer.indexLogs(launch.getId(), updatedItems);
			}
		}
	}

	private Set<AnalyzedItemRs> analyze(List<IndexTestItem> rqTestItems, Launch launch) {
		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProject(launch.getProjectRef());
			rqLaunch.setTestItems(rqTestItems);
			return analyzerServiceClient.analyze(rqLaunch);
		}
		return Collections.emptySet();
	}

	/**
	 * Filter items with logs greater than {@link LogLevel#ERROR} level
	 * and convert them to {@link IndexTestItem} analyzer model
	 *
	 * @param testItems Test items for preparing
	 * @return Prepared items for analyzer
	 */
	private List<IndexTestItem> prepareItems(List<TestItem> testItems) {
		return testItems.stream()
				.map(it -> AnalyzerUtils.fromTestItem(it, logRepository.findGreaterOrEqualLevel(it.getId(), LogLevel.ERROR)))
				.filter(it -> !isEmpty(it.getLogs()))
				.collect(Collectors.toList());
	}

	/**
	 * Update issue types for analyzed items
	 *
	 * @param rs        Results of analyzing
	 * @param testItems items to be updated
	 * @return List of updated items
	 */
	private List<TestItem> updateTestItems(Set<AnalyzedItemRs> rs, List<TestItem> testItems) {
		return rs.stream().map(analyzed -> {
			Optional<TestItem> toUpdate = testItems.stream().filter(item -> item.getId().equals(analyzed.getItemId())).findFirst();
			toUpdate.ifPresent(testItem -> {
				TestItemIssue issue = new TestItemIssue(analyzed.getIssueType(), null, true);
				ofNullable(analyzed.getRelevantItemId()).ifPresent(relevantItemId -> fromRelevantItem(issue, relevantItemId));
				testItem.setIssue(issue);
			});
			return toUpdate;
		}).filter(Optional::isPresent).map(Optional::get).collect(toList());
	}

	/**
	 * Updates issue with values are taken from most relevant item
	 *
	 * @param issue          Issue to update
	 * @param relevantItemId Relevant item id
	 */
	private void fromRelevantItem(TestItemIssue issue, String relevantItemId) {
		TestItem relevantItem = testItemRepository.findOne(relevantItemId);
		if (relevantItem != null && relevantItem.getIssue() != null) {
			issue.setIssueDescription(relevantItem.getIssue().getIssueDescription());
			issue.setExternalSystemIssues(relevantItem.getIssue().getExternalSystemIssues());
		}
	}

	/**
	 * Updates issues of investigated item and recalculates
	 * the launch's statistics
	 *
	 * @param items  Items for update
	 * @param launch Launch of investigated items
	 */
	private void saveUpdatedItems(List<TestItem> items, Launch launch) {
		Map<String, TestItemIssue> forUpdate = items.stream().collect(toMap(TestItem::getId, TestItem::getIssue));
		if (!forUpdate.isEmpty()) {
			testItemRepository.updateItemsIssues(forUpdate);
			Project project = projectRepository.findByName(launch.getProjectRef());
			statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
					.recalculateStatistics(launch);
		}
	}
}
