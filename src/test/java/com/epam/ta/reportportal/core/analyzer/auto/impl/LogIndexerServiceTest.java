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

import com.epam.ta.reportportal.core.analyzer.auto.client.IndexerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.indexer.BatchLogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.indexer.IndexerStatusCache;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.analyzer.IndexLaunch;
import com.epam.ta.reportportal.ws.model.analyzer.IndexRs;
import com.epam.ta.reportportal.ws.model.analyzer.IndexRsIndex;
import com.epam.ta.reportportal.ws.model.analyzer.IndexRsItem;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.entity.AnalyzeMode.ALL_LAUNCHES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link LogIndexerService}
 *
 * @author Ivan Sharamet
 */
class LogIndexerServiceTest {

	private BatchLogIndexer batchLogIndexer = mock(BatchLogIndexer.class);

	private TaskExecutor taskExecutor = new SyncTaskExecutor();

	private IndexerServiceClient indexerServiceClient = mock(IndexerServiceClient.class);

	private LaunchRepository launchRepository = mock(LaunchRepository.class);

	private TestItemRepository testItemRepository = mock(TestItemRepository.class);

	private LogRepository logRepository = mock(LogRepository.class);

	private IndexerStatusCache indexerStatusCache = mock(IndexerStatusCache.class);

	private LaunchPreparerService launchPreparerService = mock(LaunchPreparerService.class);

	private LogIndexerService logIndexerService = new LogIndexerService(batchLogIndexer,
			taskExecutor,
			launchRepository,
			testItemRepository,
			indexerServiceClient,
			launchPreparerService,
			indexerStatusCache
	);

	@Test
	void testIndexWithZeroCount() {
		Long launchId = 2L;
		final IndexLaunch indexLaunch = new IndexLaunch();
		indexLaunch.setLaunchId(launchId);
		when(batchLogIndexer.index(eq(1L), any(AnalyzerConfig.class))).thenReturn(0L);
		Long result = logIndexerService.index(1L, analyzerConfig()).join();
		assertThat(result, org.hamcrest.Matchers.equalTo(0L));
		verify(indexerStatusCache, times(1)).indexingFinished(1L);
	}

	@Test
	void testIndexDefectsUpdate() {
		final Map<Long, String> toUpdate = Maps.newHashMap(1L, "pb001");
		when(indexerServiceClient.indexDefectsUpdate(1L, toUpdate)).thenReturn(Collections.emptyList());
		logIndexerService.indexDefectsUpdate(1L, new AnalyzerConfig(), Lists.newArrayList(createTestItem(1L, TestItemIssueGroup.PRODUCT_BUG)));
		verify(indexerServiceClient, times(1)).indexDefectsUpdate(1L, toUpdate);
	}

	@Test
	void testIndexItemsRemove() {
		List<Long> list = Lists.newArrayList(1L);
		doNothing().when(indexerServiceClient).indexItemsRemoveAsync(1L, list);
		logIndexerService.indexItemsRemoveAsync(1L, list);
		verify(indexerServiceClient, times(1)).indexItemsRemoveAsync(1L, list);
	}

	private AnalyzerConfig analyzerConfig() {
		AnalyzerConfig analyzerConfig = new AnalyzerConfig();
		analyzerConfig.setAnalyzerMode(ALL_LAUNCHES.getValue());
		return analyzerConfig;
	}

	private Launch createLaunch(Long id) {
		Launch l = new Launch();
		l.setId(id);
		l.setMode(LaunchModeEnum.DEFAULT);
		l.setName("launch" + id);
		return l;
	}

	private TestItem createTestItem(Long id, TestItemIssueGroup issueGroup) {
		TestItem ti = new TestItem();
		ti.setItemId(id);
		ti.setLaunchId(id);
		ti.setItemResults(new TestItemResults());
		IssueType issueType = new IssueType();
		issueType.setLocator(issueGroup.getLocator());
		IssueEntity issueEntity = new IssueEntity();
		issueEntity.setIssueType(issueType);
		issueEntity.setIgnoreAnalyzer(false);
		ti.getItemResults().setIssue(issueEntity);
		return ti;
	}

	private Log createLog(Long id) {
		Log l = new Log();
		l.setId(id);
		l.setTestItem(new TestItem(id));
		l.setLogLevel(LogLevel.ERROR.toInt());
		return l;
	}

	private List<TestItem> createTestItems(int count, TestItemIssueGroup issueGroup) {
		List<TestItem> testItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			testItems.add(createTestItem((long) i, issueGroup));
		}
		return testItems;
	}

	private IndexRs createIndexRs(int count) {
		IndexRs rs = new IndexRs();
		rs.setTook(100);
		rs.setErrors(false);
		List<IndexRsItem> rsItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			IndexRsItem rsItem = new IndexRsItem();
			rsItem.setIndex(new IndexRsIndex());
			rsItems.add(rsItem);
		}
		rs.setItems(rsItems);
		return rs;
	}

}
