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

package com.epam.ta.reportportal.core.analyzer.impl;

import com.epam.ta.reportportal.core.analyzer.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.IssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexTestItem;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.IssueTypeRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.IssueEntityBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static com.epam.ta.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static com.google.common.base.Strings.nullToEmpty;
import static freemarker.template.utility.StringUtil.emptyToNull;
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

	@Autowired
	private IssueTypeHandler issueTypeHandler;

	private final TestItemRepository testItemRepository;

	private final MessageBus messageBus;

	private final LogIndexer logIndexer;

	private static final Logger LOGGER = LogManager.getLogger(com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient.class.getSimpleName());

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

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	@Transactional
	public Runnable analyze(Launch launch, AnalyzerConfig analyzerConfig) {
		return () -> new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				runAnalyzer(launch, analyzerConfig);
			}
		});
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runAnalyzer(Launch launch, AnalyzerConfig analyzerConfig) {
		if (launch != null) {
			try {
				analyzerStatusCache.analyzeStarted(launch.getId(), launch.getProjectId());
				List<TestItem> toInvestigate = testItemRepository.selectItemsInIssueByLaunch(launch.getId(), TO_INVESTIGATE.getLocator());
				List<IndexTestItem> indexTestItems = prepareItems(toInvestigate);
				IndexLaunch rqLaunch = prepareLaunch(indexTestItems, launch, launch.getProjectId(), analyzerConfig);
				Map<String, List<AnalyzedItemRs>> analyzedMap = analyzerServiceClient.analyze(rqLaunch);
				if (!MapUtils.isEmpty(analyzedMap)) {
					List<TestItem> updatedItems = analyzedMap.entrySet()
							.stream()
							.flatMap(it -> updateTestItems(it.getKey(), it.getValue(), toInvestigate, launch.getProjectId()).stream())
							.collect(toList());
					//						logIndexer.indexLogs(launch.getId(), updatedItems);
				}
			} catch (Exception e) {
				e.printStackTrace();
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

	private IndexLaunch prepareLaunch(List<IndexTestItem> rqTestItems, Launch launch, Long projectId, AnalyzerConfig analyzerConfig) {
		if (!rqTestItems.isEmpty()) {
			IndexLaunch rqLaunch = new IndexLaunch();
			rqLaunch.setLaunchId(launch.getId());
			rqLaunch.setLaunchName(launch.getName());
			rqLaunch.setProjectId(projectId);

			//uses provided analyze mode because it could be run with another mode from launch view
			//			analyzerConfig.setAnalyzerMode(analyzeMode.getValue());

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

		List<TestItem> updatedItems = rs.stream().map(analyzed -> {
			Optional<TestItem> toUpdate = testItems.stream().filter(item -> item.getItemId().equals(analyzed.getItemId())).findAny();
			toUpdate.ifPresent(testItem -> {
				TestItemActivityResource before = TO_ACTIVITY_RESOURCE.apply(testItem);
				updateTestItemIssue(projectId, analyzed, testItem);
				TestItemActivityResource after = TO_ACTIVITY_RESOURCE.apply(testItem);
				//				messageBus.publishActivity(new ItemIssueTypeDefinedEvent(before, after, analyzerInstance));
				//				messageBus.publishActivity(new LinkTicketEvent(before, after, analyzerInstance));
			});
			return toUpdate;
		}).filter(Optional::isPresent).map(Optional::get).collect(toList());
		return updatedItems;
	}

	/**
	 * Updates issue for a specified test item
	 *
	 * @param projectId - Project id
	 * @param rs        - Response from an analyzer
	 * @param testItem  - Test item to be updated
	 * @return Updated issue entity
	 */
	private IssueEntity updateTestItemIssue(Long projectId, AnalyzedItemRs rs, TestItem testItem) {
		IssueType issueType = issueTypeHandler.defineIssueType(testItem.getItemId(), projectId, rs.getLocator());
		IssueEntity issueEntity = new IssueEntityBuilder(testItem.getItemResults().getIssue()).addIssueType(issueType)
				.addIgnoreFlag(testItem.getItemResults().getIssue().getIgnoreAnalyzer())
				.addAutoAnalyzedFlag(true)
				.get();
		issueEntity.setIssueId(testItem.getItemId());
		testItem.getItemResults().setIssue(issueEntity);
		ofNullable(rs.getRelevantItemId()).ifPresent(relevantItemId -> updateIssueFromRelevantItem(issueEntity, relevantItemId));
		return issueEntity;
	}

	/**
	 * Updates issue with values are taken from most relevant item
	 *
	 * @param issue          Issue to update
	 * @param relevantItemId Relevant item id
	 */
	private void updateIssueFromRelevantItem(IssueEntity issue, Long relevantItemId) {
		TestItem relevantItem = testItemRepository.findById(relevantItemId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, relevantItemId));
		if (relevantItem.getItemResults().getIssue() != null) {
			issue.setIssueDescription(emptyToNull(nullToEmpty(issue.getIssueDescription()) + nullToEmpty(relevantItem.getItemResults()
					.getIssue()
					.getIssueDescription())));
			//TODO add external issues
			//issue.setExternalSystemIssues(Optional.ofNullable(relevantItem.getIssue().getExternalSystemIssues()).orElse(emptySet()));
		}
	}
}
