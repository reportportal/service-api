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
import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.item.impl.IssueTypeHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.entity.AnalyzeMode.ALL_LAUNCHES;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author Pavel Bortnik
 */
class IssuesAnalyzerServiceTest {

	private AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private IssueTypeHandler issueTypeHandler = mock(IssueTypeHandler.class);

	private TestItemRepository testItemRepository = mock(TestItemRepository.class);

	private MessageBus messageBus = mock(MessageBus.class);

	private LogRepository logRepository = mock(LogRepository.class);

	private LogIndexer logIndexer = mock(LogIndexer.class);

	private AnalyzerStatusCache analyzerStatusCache = mock(AnalyzerStatusCache.class);

	private IssuesAnalyzerServiceImpl issuesAnalyzer = new IssuesAnalyzerServiceImpl(analyzerStatusCache,
			analyzerServiceClient,
			logRepository,
			issueTypeHandler,
			testItemRepository,
			messageBus,
			logIndexer
	);

	@Test
	void hasAnalyzers() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		assertTrue(issuesAnalyzer.hasAnalyzers());
	}

	@Test
	void analyzeWithoutLogs() {
		Launch launch = launch();
		TestItem testItems = testItemsTI(1).get(0);

		when(logRepository.findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(singletonList(testItems.getItemId()),
				LogLevel.ERROR.toInt()
		)).thenReturn(Collections.emptyList());

		when(testItemRepository.findAllById(singletonList(1L))).thenReturn(singletonList(testItems));

		Project project = project();

		doNothing().when(analyzerStatusCache).analyzeStarted(launch.getId(), launch.getProjectId());
		doNothing().when(analyzerStatusCache).analyzeFinished(launch.getId());

		issuesAnalyzer.analyze(launch, singletonList(1L), analyzerConfig()).join();

		verify(logRepository, times(1)).findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(singletonList(testItems.getItemId()),
				LogLevel.ERROR.toInt()
		);
		verify(analyzerStatusCache, times(1)).analyzeStarted(launch.getId(), project.getId());
		verify(analyzerStatusCache, times(1)).analyzeFinished(launch.getId());
		verifyZeroInteractions(analyzerServiceClient);
	}

	@Test
	void analyze() {
		int itemsCount = 2;

		Launch launch = launch();

		List<TestItem> items = testItemsTI(itemsCount);

		when(logRepository.findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(anyListOf(Long.class),
				eq(LogLevel.ERROR.toInt())
		)).thenReturn(errorLogs(2));

		when(testItemRepository.findAllById(anyListOf(Long.class))).thenReturn(items);

		when(analyzerServiceClient.analyze(any())).thenReturn(analyzedItems(itemsCount));

		when(issueTypeHandler.defineIssueType(anyLong(), anyLong(), eq("pb001"))).thenReturn(issueProductBug().getIssueType());

		AnalyzerConfig analyzerConfig = analyzerConfig();

		CompletableFuture<Void> analyze = issuesAnalyzer.analyze(launch,
				items.stream().map(TestItem::getItemId).collect(Collectors.toList()),
				analyzerConfig
		);

		analyze.join();

		verify(logRepository, times(itemsCount)).findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(anyListOf(Long.class),
				eq(LogLevel.ERROR.toInt())
		);
		verify(analyzerServiceClient, times(1)).analyze(any());
		verify(testItemRepository, times(itemsCount)).save(any());
		verify(logIndexer, times(1)).indexLogs(eq(launch.getProjectId()), eq(singletonList(launch.getId())), eq(analyzerConfig));
		verify(messageBus, times(4)).publishActivity(any());
	}

	private AnalyzerConfig analyzerConfig() {
		AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
		return analyzerConfig;
	}

	private Project project() {
		Project project = new Project();
		project.setId(1L);
		return project;
	}

	private Launch launch() {
		Launch launch = new Launch();
		launch.setId(1L);
		launch.setName("launch");
		launch.setProjectId(1L);
		return launch;
	}

	private List<TestItem> testItemsTI(int count) {
		List<TestItem> list = new ArrayList<>(count);
		for (int i = 1; i <= count; i++) {
			TestItem test = new TestItem();
			test.setItemId((long) i);
			test.setName("test" + i);
			test.setUniqueId("unique" + i);
			test.setItemResults(new TestItemResults());
			test.getItemResults().setIssue(issueToInvestigate());
			test.getItemResults().setStatus(StatusEnum.FAILED);
			list.add(test);
		}
		return list;
	}

	private IssueEntity issueToInvestigate() {
		IssueType issueType = new IssueType();
		issueType.setLocator("ti001");
		IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIssueType(issueType);
		return issueEntity;
	}

	private IssueEntity issueProductBug() {
		IssueType issueType = new IssueType();
		issueType.setLocator("pb001");
		IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIssueType(issueType);
		return issueEntity;
	}

	private List<Log> errorLogs(int count) {
		List<Log> list = new ArrayList<>(count);
		for (int i = 1; i <= count; i++) {
			Log log = new Log();
			log.setLogMessage("Error message " + i);
			log.setLogLevel(LogLevel.ERROR.toInt());
			list.add(log);
		}
		return list;
	}

	private Map<String, List<AnalyzedItemRs>> analyzedItems(int itemsCount) {
		Map<String, List<AnalyzedItemRs>> res = new HashMap<>();
		List<AnalyzedItemRs> list = new ArrayList<>();
		for (int i = 1; i <= itemsCount; i++) {
			AnalyzedItemRs testItem = new AnalyzedItemRs();
			testItem.setItemId((long) i);
			testItem.setLocator(PRODUCT_BUG.getLocator());
			list.add(testItem);
		}
		res.put("test", list);
		return res;
	}
}