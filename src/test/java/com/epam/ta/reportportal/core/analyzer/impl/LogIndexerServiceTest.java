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
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexRsIndex;
import com.epam.ta.reportportal.core.analyzer.model.IndexRsItem;
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
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.entity.AnalyzeMode.ALL_LAUNCHES;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link LogIndexerService}
 *
 * @author Ivan Sharamet
 */
public class LogIndexerServiceTest {

	private AnalyzerServiceClient analyzerServiceClient = mock(AnalyzerServiceClient.class);

	private LaunchRepository launchRepository = mock(LaunchRepository.class);

	private TestItemRepository testItemRepository = mock(TestItemRepository.class);

	private LogRepository logRepository = mock(LogRepository.class);

	private LogIndexerService logIndexerService = new LogIndexerService(testItemRepository,
			launchRepository,
			analyzerServiceClient,
			logRepository
	);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testIndexLogWithoutTestItem() {
		Log log = createLog(1L);
		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());
		logIndexerService.indexLog(log).exceptionally(it -> null).join();
		verify(testItemRepository).findById(eq(log.getTestItem().getItemId()));
		verifyZeroInteractions(launchRepository, logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLogWithoutLaunch() {
		Log log = createLog(2L);
		TestItem ti = createTestItem(2L, PRODUCT_BUG);
		ti.setLaunch(new Launch(2L));
		log.setTestItem(ti);
		when(testItemRepository.findById(log.getTestItem().getItemId())).thenReturn(Optional.of(ti));
		when(launchRepository.findById(eq(ti.getLaunch().getId()))).thenReturn(Optional.empty());
		logIndexerService.indexLog(log).exceptionally(it -> null).join();
		verify(testItemRepository).findById(log.getTestItem().getItemId());
		verify(launchRepository).findById(eq(ti.getLaunch().getId()));
		verifyZeroInteractions(logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLog() {
		Log log = createLog(3L);
		TestItem ti = createTestItem(3L, PRODUCT_BUG);
		Launch launch = createLaunch(3L);
		ti.setLaunch(launch);
		log.setTestItem(ti);
		when(testItemRepository.findById(log.getId())).thenReturn(Optional.of(ti));
		when(launchRepository.findById(launch.getId())).thenReturn(Optional.of(launch));
		logIndexerService.indexLog(log).join();

		verify(testItemRepository).findById(ti.getItemId());
		verify(launchRepository).findById(launch.getId());
		verify(analyzerServiceClient).index(any());
		verifyZeroInteractions(logRepository);
	}

	@Test
	public void testIndexLogsWithNonExistentLaunchId() {
		Long launchId = 1L;
		when(launchRepository.findById(launchId)).thenReturn(Optional.empty());
		Long result = logIndexerService.indexLogs(Collections.singletonList(launchId), analyzerConfig()).exceptionally(it -> 0L).join();
		Assert.assertThat(result, org.hamcrest.Matchers.equalTo(0L));
		verifyZeroInteractions(logRepository);
	}

	@Test
	public void testIndexLogsWithoutTestItems() {
		Long launchId = 2L;
		when(launchRepository.findById(launchId)).thenReturn(Optional.of(createLaunch(launchId)));
		Long result = logIndexerService.indexLogs(Collections.singletonList(launchId), analyzerConfig()).join();
		Assert.assertThat(result, org.hamcrest.Matchers.equalTo(0L));
		verifyZeroInteractions(logRepository);
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
		ti.setLaunch(new Launch(id));
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
