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
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.TestItemIssueGroup;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.PRODUCT_BUG;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.TO_INVESTIGATE;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link LogIndexerService}
 *
 * @author Ivan Sharamet
 */
public class LogIndexerServiceTest {

	@Mock
	private AnalyzerServiceClient analyzerServiceClient;
	@Mock
	private LaunchRepository launchRepository;
	@Mock
	private TestItemRepository testItemRepository;
	@Mock
	private LogRepository logRepository;

	private LogIndexerService logIndexerService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		logIndexerService = new LogIndexerService(testItemRepository, launchRepository, analyzerServiceClient, logRepository);
	}

	@Test
	public void testIndexLogWithoutTestItem() {
		Log log = createLog(1L);
		when(testItemRepository.findById(1L)).thenReturn(Optional.empty());
		logIndexerService.indexLog(log);
		verify(testItemRepository).findById(eq(log.getTestItem().getItemId()));
		verifyZeroInteractions(launchRepository, logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLogWithoutLaunch() {
		Log log = createLog(2L);
		TestItem ti = createTestItem(2L, PRODUCT_BUG);
		log.setTestItem(ti);
		when(testItemRepository.findById(log.getTestItem().getItemId())).thenReturn(Optional.of(ti));
		when(launchRepository.findById(eq(ti.getLaunch().getId()))).thenReturn(null);
		logIndexerService.indexLog(log);
		verify(testItemRepository).findById(log.getTestItem().getItemId());
		verify(launchRepository).findById(eq(ti.getLaunch().getId()));
		verifyZeroInteractions(logRepository, analyzerServiceClient);
	}

	//
	//	@Test
	//	public void testIndexLog() {
	//		Log log = createLog("3");
	//		TestItem ti = createTestItem("3");
	//		Launch launch = createLaunch("3");
	//		when(testItemRepository.findOne(eq(log.getTestItemRef()))).thenReturn(ti);
	//		when(launchRepository.findOne(eq(ti.getLaunchRef()))).thenReturn(launch);
	//		logIndexerService.indexLog(log);
	//		verify(testItemRepository).findOne(eq(log.getTestItemRef()));
	//		verify(launchRepository).findOne(eq(ti.getLaunchRef()));
	//		verify(analyzerServiceClient).index(anyListOf(IndexLaunch.class));
	//		verifyZeroInteractions(mongoOperations, logRepository);
	//	}
	//
	//	@Test
	//	public void testIndexLogsWithNonExistentLaunchId() {
	//		String launchId = "1";
	//		when(launchRepository.findOne(eq(launchId))).thenReturn(null);
	//		logIndexerService.indexLogs(launchId, createTestItems(1));
	//		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	//	}
	//
	//	@Test
	//	public void testIndexLogsWithoutTestItems() {
	//		String launchId = "2";
	//		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
	//		logIndexerService.indexLogs(launchId, Collections.emptyList());
	//		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	//	}
	//
	//	@Test
	//	public void testIndexLogsTestItemsWithoutLogs() {
	//		String launchId = "3";
	//		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
	//		when(logRepository.findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR))).thenReturn(Collections.emptyList());
	//		int testItemCount = 10;
	//		logIndexerService.indexLogs(launchId, createTestItems(testItemCount));
	//		verify(logRepository, times(testItemCount)).findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR));
	//		verifyZeroInteractions(mongoOperations, analyzerServiceClient);
	//	}
	//
	//	@Test
	//	public void testIndexLogs() {
	//		String launchId = "4";
	//		Launch launch = createLaunch(launchId);
	//		when(launchRepository.findOne(eq(launchId))).thenReturn(launch);
	//		when(logRepository.findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR))).thenReturn(
	//				Collections.singletonList(createLog("id")));
	//		int testItemCount = 2;
	//		when(projectRepository.findOne(launch.getProjectRef())).thenReturn(new Project());
	//		when(analyzerServiceClient.index(anyListOf(IndexLaunch.class))).thenReturn(Collections.singletonList(createIndexRs(testItemCount)));
	//		logIndexerService.indexLogs(launchId, createTestItems(testItemCount));
	//		verify(logRepository, times(testItemCount)).findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR));
	//		verify(analyzerServiceClient).index(anyListOf(IndexLaunch.class));
	//		verifyZeroInteractions(mongoOperations);
	//	}
	//
	//	@Test
	//	public void testIndexWithIgnoreFlag() {
	//		String launchId = "5";
	//		TestItem testItem = createTestItem("id");
	//		TestItemIssue issue = testItem.getIssue();
	//		issue.setIgnoreAnalyzer(true);
	//		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
	//		logIndexerService.indexLogs(launchId, Collections.singletonList(testItem));
	//		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	//	}
	//
	//	@Test
	//
	//	public void testIndexInDebug() {
	//		String launchId = "6";
	//		Launch launch = createLaunch(launchId);
	//		launch.setMode(Mode.DEBUG);
	//		when(launchRepository.findOne(eq(launchId))).thenReturn(launch);
	//		logIndexerService.indexLogs(launchId, Collections.emptyList());
	//		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	//	}
	//
	//	@Test(expected = ReportPortalException.class)
	//	public void testIndexAllLogsNegative() {
	//		DBCollection checkpointColl = mock(DBCollection.class);
	//		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
	//		when(analyzerServiceClient.hasClients()).thenReturn(false);
	//		logIndexerService.indexAllLogs();
	//		verify(analyzerServiceClient, times(1)).hasClients();
	//		verifyZeroInteractions(testItemRepository, launchRepository, logRepository);
	//	}
	//
	//	@Test
	//	public void testIndexAllLogsWithoutLogs() {
	//		DBCollection checkpointColl = mock(DBCollection.class);
	//		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
	//		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
	//		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
	//		when(analyzerServiceClient.hasClients()).thenReturn(true);
	//		logIndexerService.indexAllLogs();
	//		verifyZeroInteractions(launchRepository, testItemRepository);
	//		verify(analyzerServiceClient, times(0)).index(any());
	//		verify(analyzerServiceClient, times(1)).hasClients();
	//	}
	//
	//	@Test
	//	public void testIndexAllLogsWithoutLaunches() {
	//		DBCollection checkpointColl = mock(DBCollection.class);
	//		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
	//		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
	//		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
	//		when(launchRepository.findOne(anyString())).thenReturn(null);
	//		when(analyzerServiceClient.hasClients()).thenReturn(true);
	//		logIndexerService.indexAllLogs();
	//		verifyZeroInteractions(testItemRepository);
	//		verify(analyzerServiceClient, times(0)).index(any());
	//		verify(analyzerServiceClient, times(1)).hasClients();
	//	}
	//
	//	@Test
	//	public void testIndexAllLogsWithoutTestItems() {
	//		DBCollection checkpointColl = mock(DBCollection.class);
	//		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
	//		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
	//		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
	//		when(launchRepository.findOne(anyString())).thenReturn(createLaunch("launchId"));
	//		when(testItemRepository.findOne(anyString())).thenReturn(createTestItem("testItemId"));
	//		when(analyzerServiceClient.hasClients()).thenReturn(true);
	//		logIndexerService.indexAllLogs();
	//		verify(analyzerServiceClient, times(1)).hasClients();
	//		verify(analyzerServiceClient, times(0)).index(any());
	//	}
	//
	//	@Test
	//	public void testIndexAllLogs() {
	//		DBCollection checkpointColl = mock(DBCollection.class);
	//		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
	//		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
	//		int batchCount = 5;
	//		int logCount = batchCount * BATCH_SIZE;
	//		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(logCount));
	//		when(launchRepository.findOne(anyString())).thenReturn(createLaunch("launchId"));
	//		when(testItemRepository.findOne(anyString())).thenReturn(createTestItem("testItemId"));
	//		when(analyzerServiceClient.hasClients()).thenReturn(true);
	//		logIndexerService.indexAllLogs();
	//		verify(checkpointColl, times(batchCount)).save(any(DBObject.class));
	//		verify(analyzerServiceClient, times(batchCount)).index(anyListOf(IndexLaunch.class));
	//	}
	//
	//	@Test
	//	public void testIndexTIItems() {
	//		DBCollection checkpointColl = mock(DBCollection.class);
	//		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
	//		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
	//		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(5));
	//		when(testItemRepository.findOne(anyString())).thenReturn(createToInvestigateItem("testItemId"));
	//		when(analyzerServiceClient.hasClients()).thenReturn(true);
	//		logIndexerService.indexAllLogs();
	//		verify(checkpointColl, times(0)).save(any(DBObject.class));
	//		verify(analyzerServiceClient, times(0)).index(anyListOf(IndexLaunch.class));
	//	}
	//
	private Launch createLaunch(Long id) {
		Launch l = new Launch();
		l.setId(id);
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

	private List<TestItem> createTestItems(int count) {
		List<TestItem> testItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			testItems.add(createTestItem((long) i, TO_INVESTIGATE));
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
