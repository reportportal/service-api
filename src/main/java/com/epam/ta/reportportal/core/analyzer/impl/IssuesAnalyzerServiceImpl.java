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

import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.IssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.events.activity.LinkTicketEvent;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.AnalyzeMode;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.converters.IssueConverter;
import com.epam.ta.reportportal.ws.converter.converters.TestItemConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.TestItemResource;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils.getAnalyzerConfig;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Default implementation of {@link IssuesAnalyzer}.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service
public class IssuesAnalyzerServiceImpl implements IssuesAnalyzer {

	private final AnalyzerStatusCache analyzerStatusCache;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final LogRepository logRepository;

	private final IssueTypeRepository issueTypeRepository;

	private final TestItemRepository testItemRepository;

	private final MessageBus messageBus;

	private final LogIndexer logIndexer;

	@Autowired
	public IssuesAnalyzerServiceImpl(AnalyzerStatusCache analyzerStatusCache, AnalyzerServiceClient analyzerServiceClient,
			LogRepository logRepository, IssueTypeRepository issueTypeRepository, TestItemRepository testItemRepository,
			MessageBus messageBus, LogIndexer logIndexer) {
		this.analyzerStatusCache = analyzerStatusCache;
		this.analyzerServiceClient = analyzerServiceClient;
		this.logRepository = logRepository;
		this.issueTypeRepository = issueTypeRepository;
		this.testItemRepository = testItemRepository;
		this.messageBus = messageBus;
		this.logIndexer = logIndexer;
	}

	@Override
	public void analyze(Launch launch, Project project, List<TestItem> testItems, AnalyzeMode mode) {
		if (launch != null) {
			try {
				analyzerStatusCache.analyzeStarted(launch.getId(), project.getName());
				List<IndexTestItem> indexTestItems = prepareItems(testItems);
				IndexLaunch rqLaunch = prepareLaunch(indexTestItems, launch, project, mode);
				analyzerServiceClient.analyze(rqLaunch).thenAcceptAsync(map -> {
					if (!MapUtils.isEmpty(map)) {
						List<TestItem> updatedItems = map.entrySet()
								.stream()
								.flatMap(it -> updateTestItems(it.getKey(), it.getValue(), testItems, launch.getProjectId()).stream())
								.collect(toList());
						testItemRepository.saveAll(updatedItems);
						logIndexer.indexLogs(launch.getId(), updatedItems);
					}
				});
			} finally {
				analyzerStatusCache.analyzeFinished(launch.getId());
			}

		}
	}

	@Override
	public boolean hasAnalyzers() {
		return analyzerServiceClient.hasClients();
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
				.map(it -> AnalyzerUtils.fromTestItem(it,
						logRepository.findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(singletonList(it.getItemId()),
								LogLevel.ERROR.toInt()
						)
				))
				.filter(it -> !isEmpty(it.getLogs()))
				.collect(Collectors.toList());
	}

	private IndexLaunch prepareLaunch(List<IndexTestItem> rqTestItems, Launch launch, Project project, AnalyzeMode analyzeMode) {
		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProjectId(project.getId());

			AnalyzerConfig analyzerConfig = getAnalyzerConfig(project);
			//uses provided analyze mode because it could be run with another mode from launch view
			analyzerConfig.setAnalyzerMode(analyzeMode.getValue());

			rqLaunch.setAnalyzerConfig(analyzerConfig);
			rqLaunch.setTestItems(rqTestItems);
			return rqLaunch;
		}
		return null;
	}

	/**
	 * Update issue types for analyzed items and posted events for updated
	 *
	 * @param rs        Results of analyzing
	 * @param testItems items to be updated
	 * @return List of updated items
	 */
	private List<TestItem> updateTestItems(String analyzerInstance, List<AnalyzedItemRs> rs, List<TestItem> testItems, Long projectId) {
		final Map<IssueDefinition, TestItem> forEvents = new HashMap<>();
		final Map<String, TestItemResource> relevantItemIdMap = new HashMap<>();

		List<TestItem> beforeUpdate = new ArrayList<>(rs.size());
		List<TestItem> updatedItems = rs.stream().map(analyzed -> {
			Optional<TestItem> toUpdate = testItems.stream().filter(item -> item.getItemId().equals(analyzed.getItemId())).findAny();
			toUpdate.ifPresent(testItem -> {
				beforeUpdate.add(SerializationUtils.clone(testItem));

				IssueEntity before = SerializationUtils.clone(testItem.getItemResults().getIssue());

				IssueEntity issueEntity = new IssueEntity();
				issueEntity.setIssueType(issueTypeRepository.findById(analyzed.getIssueTypeId())
						.orElseThrow(() -> new ReportPortalException(ErrorType.ISSUE_TYPE_NOT_FOUND, analyzed.getIssueTypeId())));
				issueEntity.setAutoAnalyzed(true);
				issueEntity.setIgnoreAnalyzer(testItem.getItemResults().getIssue().getIgnoreAnalyzer());

				ofNullable(analyzed.getRelevantItemId()).ifPresent(relevantItemId -> fromRelevantItem(issueEntity, relevantItemId));
				IssueDefinition issueDefinition = createIssueDefinition(testItem.getItemId(), issueEntity);
				forEvents.put(issueDefinition, SerializationUtils.clone(testItem));

				TestItemResource testItemResource = TestItemConverter.TO_RESOURCE.apply(testItemRepository.findById(analyzed.getItemId())
						.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, analyzed.getItemId())));
				relevantItemIdMap.put(String.valueOf(testItem.getItemId()), testItemResource);
				testItem.getItemResults().setIssue(issueEntity);
				messageBus.publishActivity(new LinkTicketEvent(
						before,
						issueEntity,
						100L,
						projectId,
						testItem.getItemId(),
						testItem.getName()
				));
			});
			return toUpdate;
		}).filter(Optional::isPresent).map(Optional::get).collect(toList());
		//		eventPublisher.publishEvent(new ItemIssueTypeDefined(forEvents, analyzerInstance, project, relevantItemIdMap));
		//		eventPublisher.publishEvent(new TicketAttachedEvent(beforeUpdate, updatedItems, analyzerInstance, project, relevantItemIdMap));
		forEvents.forEach((key, value) -> messageBus.publishActivity(new ItemIssueTypeDefinedEvent(100L, key, value, projectId)));
		return updatedItems;
	}

	private IssueDefinition createIssueDefinition(Long id, IssueEntity issue) {
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
	private void fromRelevantItem(IssueEntity issue, Long relevantItemId) {
		TestItem relevantItem = testItemRepository.findById(relevantItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, relevantItemId));
		if (relevantItem.getItemResults().getIssue() != null) {
			issue.setIssueDescription(emptyToNull(nullToEmpty(issue.getIssueDescription()) + nullToEmpty(relevantItem.getItemResults()
					.getIssue()
					.getIssueDescription())));
			//issue.setExternalSystemIssues(Optional.ofNullable(relevantItem.getIssue().getExternalSystemIssues()).orElse(emptySet()));
		}
	}
}
