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

import com.epam.ta.reportportal.core.analyzer.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.analyzer.model.IndexRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexRsIndex;
import com.epam.ta.reportportal.core.analyzer.model.IndexRsItem;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.epam.ta.reportportal.core.analyzer.impl.LogIndexerService.BATCH_SIZE;
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
	private MongoOperations mongoOperations;
	@Mock
	private LaunchRepository launchRepository;
	@Mock
	private TestItemRepository testItemRepository;
	@Mock
	private LogRepository logRepository;

	@InjectMocks
	private LogIndexerService logIndexerService;

	@Before
	public void setup() {
		RetryTemplate retrier = new RetryTemplate();
		TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
		timeoutRetryPolicy.setTimeout(TimeUnit.SECONDS.toMillis(2L));
		retrier.setRetryPolicy(timeoutRetryPolicy);
		retrier.setBackOffPolicy(new FixedBackOffPolicy());
		retrier.setThrowLastExceptionOnExhausted(true);

		logIndexerService = new LogIndexerService();
		logIndexerService.setRetrier(retrier);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIndexLogWithoutTestItem() {
		Log log = createLog("1");
		when(testItemRepository.findOne(anyString())).thenReturn(null);
		logIndexerService.indexLog(log);
		verify(testItemRepository).findOne(eq(log.getTestItemRef()));
		verifyZeroInteractions(mongoOperations, launchRepository, logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLogWithoutLaunch() {
		Log log = createLog("2");
		TestItem ti = createTestItem("2");
		when(testItemRepository.findOne(eq(log.getTestItemRef()))).thenReturn(ti);
		when(launchRepository.findOne(eq(ti.getLaunchRef()))).thenReturn(null);
		logIndexerService.indexLog(log);
		verify(testItemRepository).findOne(eq(log.getTestItemRef()));
		verify(launchRepository).findOne(eq(ti.getLaunchRef()));
		verifyZeroInteractions(mongoOperations, logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLog() {
		Log log = createLog("3");
		TestItem ti = createTestItem("3");
		Launch launch = createLaunch("3");
		when(testItemRepository.findOne(eq(log.getTestItemRef()))).thenReturn(ti);
		when(launchRepository.findOne(eq(ti.getLaunchRef()))).thenReturn(launch);
		logIndexerService.indexLog(log);
		verify(testItemRepository).findOne(eq(log.getTestItemRef()));
		verify(launchRepository).findOne(eq(ti.getLaunchRef()));
		verify(analyzerServiceClient).index(anyListOf(IndexLaunch.class));
		verifyZeroInteractions(mongoOperations, logRepository);
	}

	@Test
	public void testIndexLogsWithNonExistentLaunchId() {
		String launchId = "1";
		when(launchRepository.findOne(eq(launchId))).thenReturn(null);
		logIndexerService.indexLogs(launchId, createTestItems(1));
		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLogsWithoutTestItems() {
		String launchId = "2";
		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
		logIndexerService.indexLogs(launchId, Collections.emptyList());
		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	}

	@Test
	public void testIndexLogsTestItemsWithoutLogs() {
		String launchId = "3";
		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
		when(logRepository.findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR))).thenReturn(Collections.emptyList());
		int testItemCount = 10;
		logIndexerService.indexLogs(launchId, createTestItems(testItemCount));
		verify(logRepository, times(testItemCount)).findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR));
		verifyZeroInteractions(mongoOperations, analyzerServiceClient);
	}

	@Test
	public void testIndexLogs() {
		String launchId = "4";
		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
		when(logRepository.findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR))).thenReturn(
				Collections.singletonList(createLog("id")));
		int testItemCount = 2;
		when(analyzerServiceClient.index(anyListOf(IndexLaunch.class))).thenReturn(Collections.singletonList(createIndexRs(testItemCount)));
		logIndexerService.indexLogs(launchId, createTestItems(testItemCount));
		verify(logRepository, times(testItemCount)).findGreaterOrEqualLevel(anyListOf(String.class), eq(LogLevel.ERROR));
		verify(analyzerServiceClient).index(anyListOf(IndexLaunch.class));
		verifyZeroInteractions(mongoOperations);
	}

	@Test
	public void testIndexWithIgnoreFlag() {
		String launchId = "5";
		TestItem testItem = createTestItem("id");
		TestItemIssue issue = testItem.getIssue();
		issue.setIgnoreAnalyzer(true);
		when(launchRepository.findOne(eq(launchId))).thenReturn(createLaunch(launchId));
		logIndexerService.indexLogs(launchId, Collections.singletonList(testItem));
		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	}

	@Test

	public void testIndexInDebug() {
		String launchId = "6";
		Launch launch = createLaunch(launchId);
		launch.setMode(Mode.DEBUG);
		when(launchRepository.findOne(eq(launchId))).thenReturn(launch);
		logIndexerService.indexLogs(launchId, Collections.emptyList());
		verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
	}

	@Test(expected = ReportPortalException.class)
	public void testIndexAllLogsNegative() {
		DBCollection checkpointColl = mock(DBCollection.class);
		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
		when(analyzerServiceClient.hasClients()).thenReturn(false);
		logIndexerService.indexAllLogs();
		verify(analyzerServiceClient, times(1)).hasClients();
		verifyZeroInteractions(testItemRepository, launchRepository, logRepository);
	}

	@Test
	public void testIndexAllLogsWithoutLogs() {
		DBCollection checkpointColl = mock(DBCollection.class);
		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		logIndexerService.indexAllLogs();
		verifyZeroInteractions(launchRepository, testItemRepository);
		verify(analyzerServiceClient, times(0)).index(any());
		verify(analyzerServiceClient, times(1)).hasClients();
	}

	@Test
	public void testIndexAllLogsWithoutLaunches() {
		DBCollection checkpointColl = mock(DBCollection.class);
		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
		when(launchRepository.findOne(anyString())).thenReturn(null);
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		logIndexerService.indexAllLogs();
		verifyZeroInteractions(testItemRepository);
		verify(analyzerServiceClient, times(0)).index(any());
		verify(analyzerServiceClient, times(1)).hasClients();
	}

	@Test
	public void testIndexAllLogsWithoutTestItems() {
		DBCollection checkpointColl = mock(DBCollection.class);
		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
		when(launchRepository.findOne(anyString())).thenReturn(createLaunch("launchId"));
		when(testItemRepository.findOne(anyString())).thenReturn(createTestItem("testItemId"));
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		logIndexerService.indexAllLogs();
		verify(analyzerServiceClient, times(1)).hasClients();
		verify(analyzerServiceClient, times(0)).index(any());
	}

	@Test
	public void testIndexAllLogs() {
		DBCollection checkpointColl = mock(DBCollection.class);
		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
		int batchCount = 5;
		int logCount = batchCount * BATCH_SIZE;
		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(logCount));
		when(launchRepository.findOne(anyString())).thenReturn(createLaunch("launchId"));
		when(testItemRepository.findOne(anyString())).thenReturn(createTestItem("testItemId"));
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		logIndexerService.indexAllLogs();
		verify(checkpointColl, times(batchCount)).save(any(DBObject.class));
		verify(analyzerServiceClient, times(batchCount)).index(anyListOf(IndexLaunch.class));
	}

	@Test
	public void testIndexTIItems() {
		DBCollection checkpointColl = mock(DBCollection.class);
		when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
		when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
		when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(5));
		when(testItemRepository.findOne(anyString())).thenReturn(createToInvestigateItem("testItemId"));
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		logIndexerService.indexAllLogs();
		verify(checkpointColl, times(0)).save(any(DBObject.class));
		verify(analyzerServiceClient, times(0)).index(anyListOf(IndexLaunch.class));
	}

	private Launch createLaunch(String id) {
		Launch l = new Launch();
		l.setId(id);
		l.setName("launch" + id);
		return l;
	}

	private TestItem createTestItem(String id) {
		TestItem ti = new TestItem();
		ti.setId(id);
		ti.setLaunchRef("launch" + id);
		ti.setIssue(new TestItemIssue(TestItemIssueType.PRODUCT_BUG.getLocator(), null));
		return ti;
	}

	private TestItem createToInvestigateItem(String id) {
		TestItem ti = new TestItem();
		ti.setId(id);
		ti.setLaunchRef("launch" + id);
		ti.setIssue(new TestItemIssue());
		return ti;
	}

	private Log createLog(String id) {
		Log l = new Log();
		l.setId(id);
		l.setTestItemRef("testItem" + id);
		l.setLevel(LogLevel.ERROR);
		return l;
	}

	private List<TestItem> createTestItems(int count) {
		List<TestItem> testItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			testItems.add(createTestItem(String.valueOf(i)));
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

	private CloseableIterator<Log> createLogIterator(int count) {
		return new CloseableIterator<Log>() {
			private int i = count;

			@Override
			public void close() {

			}

			@Override
			public boolean hasNext() {
				return i > 0;
			}

			@Override
			public Log next() {
				i--;
				Log l = new Log();
				String id = String.valueOf(count - i);
				l.setId(id);
				l.setLevel(LogLevel.ERROR);
				l.setTestItemRef("testItem" + id);
				return l;
			}
		};
	}

}
