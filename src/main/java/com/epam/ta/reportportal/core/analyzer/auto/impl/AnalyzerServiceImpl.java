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
import com.epam.ta.reportportal.core.analyzer.auto.impl.preparer.LaunchPreparerService;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ItemIssueTypeDefinedEvent;
import com.epam.ta.reportportal.core.events.activity.LinkTicketEvent;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.activity.ActivityAction;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.analyzer.AnalyzedItemRs;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
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

import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

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

	private final LaunchPreparerService launchPreparerService;

	private final AnalyzerServiceClient analyzerServicesClient;

	private final IssueTypeHandler issueTypeHandler;

	private final TestItemRepository testItemRepository;

	private final MessageBus messageBus;

	@Autowired
	public AnalyzerServiceImpl(AnalyzerStatusCache analyzerStatusCache, LaunchPreparerService launchPreparerService,
			AnalyzerServiceClient analyzerServicesClient, IssueTypeHandler issueTypeHandler, TestItemRepository testItemRepository,
			MessageBus messageBus) {
		this.analyzerStatusCache = analyzerStatusCache;
		this.launchPreparerService = launchPreparerService;
		this.analyzerServicesClient = analyzerServicesClient;
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
			Optional<IndexLaunch> rqLaunch = launchPreparerService.prepare(launch, toAnalyze, analyzerConfig);
			rqLaunch.ifPresent(rq -> analyzeLaunch(launch, toAnalyze, rq));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			analyzerStatusCache.analyzeFinished(AUTO_ANALYZER_KEY, launch.getId());
		}
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

		RelevantItemInfo relevantItemInfo = null;
		if (rs.getRelevantItemId() != null) {
			Optional<TestItem> relevantItemOptional = testItemRepository.findById(rs.getRelevantItemId());
			if (relevantItemOptional.isPresent()) {
				relevantItemInfo = updateIssueFromRelevantItem(issueEntity, relevantItemOptional.get());
			} else {
				LOGGER.error(ErrorType.TEST_ITEM_NOT_FOUND.getDescription(), rs.getRelevantItemId());
			}
		}

		return relevantItemInfo;
	}

	/**
	 * Updates issue with values are taken from most relevant item
	 *
	 * @param issue        Issue to update
	 * @param relevantItem Relevant item
	 */
	private RelevantItemInfo updateIssueFromRelevantItem(IssueEntity issue, TestItem relevantItem) {
		ofNullable(relevantItem.getItemResults().getIssue()).ifPresent(relevantIssue -> {
			final String issueDescription = resolveDescription(issue, relevantIssue);
			issue.setIssueDescription(emptyToNull(issueDescription));
			issue.setTickets(Sets.newHashSet(relevantIssue.getTickets()));
		});

		return AnalyzerUtils.TO_RELEVANT_ITEM_INFO.apply(relevantItem);
	}

	private String resolveDescription(IssueEntity issue, IssueEntity relevantIssue) {
		return ofNullable(issue.getIssueDescription()).map(description -> String.join("\n",
				description,
				nullToEmpty(relevantIssue.getIssueDescription())
		)).orElseGet(() -> nullToEmpty(relevantIssue.getIssueDescription()));
	}
}
