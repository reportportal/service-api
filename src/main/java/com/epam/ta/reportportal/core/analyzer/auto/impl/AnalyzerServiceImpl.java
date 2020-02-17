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

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerService;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.events.activity.LinkTicketEvent;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.analyzer.AnalyzedItemRs;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexTestItem;
import com.epam.ta.reportportal.ws.model.analyzer.RelevantItemInfo;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Sets;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Default implementation of {@link AnalyzerService}.
 *
 * @author Ivan Sharamet
 * @author Pavel Bortnik
 */
@Service
@Transactional
public class AnalyzerServiceImpl implements AnalyzerService {

	private static final Logger LOGGER = LogManager.getLogger(AnalyzerServiceImpl.class.getName());

	private final AnalyzerStatusCache analyzerStatusCache;

	private final AnalyzerServiceClient analyzerServicesClient;

	private final LogRepository logRepository;

	private final IssueTypeHandler issueTypeHandler;

	private final TestItemRepository testItemRepository;

	private final MessageBus messageBus;

	@Autowired
	public AnalyzerServiceImpl(AnalyzerStatusCache analyzerStatusCache, AnalyzerServiceClient analyzerServicesClient,
			LogRepository logRepository, IssueTypeHandler issueTypeHandler, TestItemRepository testItemRepository, MessageBus messageBus) {
		this.analyzerStatusCache = analyzerStatusCache;
		this.analyzerServicesClient = analyzerServicesClient;
		this.logRepository = logRepository;
		this.issueTypeHandler = issueTypeHandler;
		this.testItemRepository = testItemRepository;
		this.messageBus = messageBus;
	}

	@Override
	public boolean hasAnalyzers() {
		return analyzerServicesClient.hasClients();
	}

	@Override
	public void runAnalyzers(Launch launch, List<Long> testItemIds, AnalyzerConfig analyzerConfig) {
		try {
			analyzerStatusCache.analyzeStarted(AUTO_ANALYZER_KEY, launch.getId(), launch.getProjectId());
			List<TestItem> toAnalyze = testItemRepository.findAllById(testItemIds);
			Optional<IndexLaunch> rqLaunch = prepareLaunch(launch, analyzerConfig, toAnalyze);
			rqLaunch.ifPresent(rq -> analyzeLaunch(launch, toAnalyze, rq));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			analyzerStatusCache.analyzeFinished(AUTO_ANALYZER_KEY, launch.getId());
		}
	}

	/**
	 * Create an {@link IndexLaunch} object to be sent to analyzers
	 *
	 * @param launch         Launch to be created from
	 * @param analyzerConfig Analyzer config
	 * @param toAnalyze      Items to be analyzed
	 * @return Optional of {@link IndexLaunch}
	 */
	private Optional<IndexLaunch> prepareLaunch(Launch launch, AnalyzerConfig analyzerConfig, List<TestItem> toAnalyze) {
		if (launch == null) {
			return Optional.empty();
		}
		List<IndexTestItem> indexTestItems = prepareItems(toAnalyze);
		if (!indexTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProjectId(launch.getProjectId());
			rqLaunch.setAnalyzerConfig(analyzerConfig);
			rqLaunch.setTestItems(indexTestItems);
			return Optional.of(rqLaunch);
		}
		return Optional.empty();
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
						logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(it.getLaunchId(),
								singletonList(it.getItemId()),
								LogLevel.ERROR.toInt()
						)
				))
				.filter(it -> !isEmpty(it.getLogs()))
				.collect(Collectors.toList());
	}

	/**
	 * Run analyzing for a concrete launch
	 *
	 * @param launch    Launch
	 * @param toAnalyze Items to analyze
	 * @param rq        Prepared rq for sending to analyzers
	 */
	private void analyzeLaunch(Launch launch, List<TestItem> toAnalyze, IndexLaunch rq) {
		LOGGER.info("Start analysis for launch with id '{}'", rq.getLaunchId());
		Map<String, List<AnalyzedItemRs>> analyzedMap = analyzerServicesClient.analyze(rq);
		if (!MapUtils.isEmpty(analyzedMap)) {
			analyzedMap.forEach((key, value) -> updateTestItems(key, value, toAnalyze, launch.getProjectId()));
		}
	}

	/**
	 * Update issue types for analyzed items and posted events for updated
	 *
	 * @param rs        Results of analyzing
	 * @param testItems items to be updated
	 * @return List of updated items
	 */
	private List<TestItem> updateTestItems(String analyzerInstance, List<AnalyzedItemRs> rs, List<TestItem> testItems, Long projectId) {
		return rs.stream().map(analyzed -> {
			Optional<TestItem> toUpdate = testItems.stream().filter(item -> item.getItemId().equals(analyzed.getItemId())).findAny();
			toUpdate.ifPresent(testItem -> {
				LOGGER.debug("Analysis has found a match: {}", analyzed);

				if (!testItem.getItemResults().getIssue().getIssueType().getLocator().equals(analyzed.getLocator())) {
					TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);
					RelevantItemInfo relevantItemInfo = updateTestItemIssue(projectId, analyzed, testItem);
					TestItemActivityResource after = TO_ACTIVITY_RESOURCE.apply(testItem, projectId);

					testItemRepository.save(testItem);
					messageBus.publishActivity(new ItemIssueTypeDefinedEvent(before, after, analyzerInstance, relevantItemInfo));
					ofNullable(after.getTickets()).ifPresent(it -> messageBus.publishActivity(new LinkTicketEvent(before,
							after,
							analyzerInstance,
							ActivityAction.LINK_ISSUE_AA
					)));
				}
			});
			return toUpdate;
		}).filter(Optional::isPresent).map(Optional::get).collect(toList());
	}

	/**
	 * Updates issue for a specified test item
	 *
	 * @param projectId - Project id
	 * @param rs        - Response from an analyzer
	 * @param testItem  - Test item to be updated
	 * @return Updated issue entity
	 */
	private RelevantItemInfo updateTestItemIssue(Long projectId, AnalyzedItemRs rs, TestItem testItem) {
		IssueType issueType = issueTypeHandler.defineIssueType(projectId, rs.getLocator());
		IssueEntity issueEntity = new IssueEntityBuilder(testItem.getItemResults().getIssue()).addIssueType(issueType)
				.addIgnoreFlag(testItem.getItemResults().getIssue().getIgnoreAnalyzer())
				.addAutoAnalyzedFlag(true)
				.get();
		issueEntity.setIssueId(testItem.getItemId());
		issueEntity.setTestItemResults(testItem.getItemResults());
		testItem.getItemResults().setIssue(issueEntity);
		return ofNullable(rs.getRelevantItemId()).map(relevantItemId -> updateIssueFromRelevantItem(issueEntity, relevantItemId))
				.orElse(null);

	}

	/**
	 * Updates issue with values are taken from most relevant item
	 *
	 * @param issue          Issue to update
	 * @param relevantItemId Relevant item id
	 */
	private RelevantItemInfo updateIssueFromRelevantItem(IssueEntity issue, Long relevantItemId) {
		TestItem relevantItem = testItemRepository.findById(relevantItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, relevantItemId));

		if (relevantItem.getItemResults().getIssue() != null) {
			issue.setIssueDescription(emptyToNull(nullToEmpty(issue.getIssueDescription()) + nullToEmpty(relevantItem.getItemResults()
					.getIssue()
					.getIssueDescription())));
			issue.setTickets(Sets.newHashSet(relevantItem.getItemResults().getIssue().getTickets()));
		}

		return AnalyzerUtils.TO_RELEVANT_ITEM_INFO.apply(relevantItem);
	}
}
