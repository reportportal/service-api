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
import com.epam.ta.reportportal.database.entity.AnalyzeMode;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import com.epam.ta.reportportal.events.TicketAttachedEvent;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Collections.singletonList;
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

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public boolean hasAnalyzers() {
		return analyzerServiceClient.hasClients();
	}

	@Override
	public void analyze(Launch launch, List<TestItem> testItems, AnalyzeMode analyzeMode) {
		if (launch != null) {
			List<IndexTestItem> rqTestItems = prepareItems(testItems);
			Map<String, List<AnalyzedItemRs>> rs = analyze(rqTestItems, launch, analyzeMode);
			if (!MapUtils.isEmpty(rs)) {
				List<TestItem> updatedItems = rs.entrySet()
						.stream()
						.flatMap(it -> updateTestItems(it.getKey(), it.getValue(), testItems, launch.getProjectRef()).stream())
						.collect(toList());
				saveUpdatedItems(updatedItems, launch);
				logIndexer.indexLogs(launch.getId(), updatedItems);
			}
		}
	}

	private Map<String, List<AnalyzedItemRs>> analyze(List<IndexTestItem> rqTestItems, Launch launch, AnalyzeMode analyzeMode) {
		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setAnalyzeMode(analyzeMode.getValue());
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProject(launch.getProjectRef());
			rqLaunch.setTestItems(rqTestItems);
			return analyzerServiceClient.analyze(rqLaunch);
		}
		return Collections.emptyMap();
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
				.map(it -> AnalyzerUtils.fromTestItem(it, logRepository.findGreaterOrEqualLevel(singletonList(it.getId()), LogLevel.ERROR)))
				.filter(it -> !isEmpty(it.getLogs()))
				.collect(Collectors.toList());
	}

	/**
	 * Update issue types for analyzed items and posted events for updated
	 *
	 * @param rs        Results of analyzing
	 * @param testItems items to be updated
	 * @return List of updated items
	 */
	private List<TestItem> updateTestItems(String analyzerInstance, List<AnalyzedItemRs> rs, List<TestItem> testItems, String project) {
		final Map<IssueDefinition, TestItem> forEvents = new HashMap<>();
		List<TestItem> beforeUpdate = new ArrayList<>(rs.size());
		List<TestItem> updatedItems = rs.stream().map(analyzed -> {
			Optional<TestItem> toUpdate = testItems.stream().filter(item -> item.getId().equals(analyzed.getItemId())).findAny();
			toUpdate.ifPresent(testItem -> {
				beforeUpdate.add(SerializationUtils.clone(testItem));

				TestItemIssue issue = new TestItemIssue(analyzed.getIssueType(), null, true);
				issue.setIgnoreAnalyzer(testItem.getIssue().isIgnoreAnalyzer());

				ofNullable(analyzed.getRelevantItemId()).ifPresent(relevantItemId -> fromRelevantItem(issue, relevantItemId));
				IssueDefinition issueDefinition = createIssueDefinition(testItem.getId(), issue);
				forEvents.put(issueDefinition, SerializationUtils.clone(testItem));
				testItem.setIssue(issue);
			});
			return toUpdate;
		}).filter(Optional::isPresent).map(Optional::get).collect(toList());
		eventPublisher.publishEvent(new ItemIssueTypeDefined(forEvents, analyzerInstance, project));
		eventPublisher.publishEvent(new TicketAttachedEvent(beforeUpdate, updatedItems, analyzerInstance, project));
		return updatedItems;
	}

	private IssueDefinition createIssueDefinition(String id, TestItemIssue issue) {
		IssueDefinition issueDefinition = new IssueDefinition();
		issueDefinition.setId(id);
		issueDefinition.setIssue(IssueConverter.TO_MODEL.apply(issue));
		return issueDefinition;
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
			issue.setIssueDescription(
					emptyToNull(nullToEmpty(issue.getIssueDescription()) + nullToEmpty(relevantItem.getIssue().getIssueDescription())));
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
