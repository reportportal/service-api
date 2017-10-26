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

import com.epam.ta.reportportal.core.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient;
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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Default implementation of {@link IIssuesAnalyzer}.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service
public class IssuesAnalyzerService implements IIssuesAnalyzer {

	@Autowired
	private AnalyzerServiceClient analyzerServiceClient;

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

	private static final Predicate<IndexTestItem> IS_ANALYZED = it -> it.getIssueType() != null;

	@Override
	public boolean hasAnalyzers() {
		return analyzerServiceClient.hasClients();
	}

	@Override
	public void analyze(Launch launch, List<TestItem> testItems) {
		if (launch != null) {
			List<IndexTestItem> rqTestItems = prepareItems(testItems);
			IndexLaunch rs = analyze(rqTestItems, launch);
			if (rs != null) {
				List<TestItem> updatedItems = updateTestItems(rs, testItems);
				saveUpdatedItems(updatedItems, launch);
				logIndexer.indexLogs(launch.getId(), updatedItems);
			}
		}
	}

	private IndexLaunch analyze(List<IndexTestItem> rqTestItems, Launch launch) {
		IndexLaunch rs = null;
		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProject(launch.getProjectRef());
			rqLaunch.setTestItems(rqTestItems);
			rs = analyzerServiceClient.analyze(rqLaunch);
		}
		return rs;
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
				.filter(it -> !CollectionUtils.isEmpty(it.getLogs()))
				.collect(Collectors.toList());
	}

	/**
	 * Update issue types for analyzed items
	 *
	 * @param rs        Results of analyzing
	 * @param testItems items to be updated
	 * @return List of updated items
	 */
	private List<TestItem> updateTestItems(IndexLaunch rs, List<TestItem> testItems) {
		return rs.getTestItems().stream().filter(IS_ANALYZED).map(indexTestItem -> {
			TestItem toUpdate = testItems.stream()
					.filter(item -> item.getId().equals(indexTestItem.getTestItemId()))
					.findFirst()
					.orElse(null);
			if (toUpdate != null) {
				toUpdate.setIssue(new TestItemIssue(indexTestItem.getIssueType(), null, true));
			}
			return toUpdate;
		}).filter(Objects::nonNull).collect(toList());
	}

	/**
	 * Updates issues of investigated item and recalculates
	 * the whole launch's statistics
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
