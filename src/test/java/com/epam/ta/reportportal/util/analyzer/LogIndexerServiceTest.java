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

package com.epam.ta.reportportal.util.analyzer;

import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexRs;
import com.epam.ta.reportportal.util.analyzer.model.IndexRsIndex;
import com.epam.ta.reportportal.util.analyzer.model.IndexRsItem;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link LogIndexerService}
 *
 * @author Ivan Sharamet
 *
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
        logIndexerService = new LogIndexerService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIndexWithNonExistentLaunchId() {
        String launchId = "1";
        when(launchRepository.findEntryById(eq(launchId))).thenReturn(null);
        logIndexerService.indexLogs(launchId, createTestItems(1));
        verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
    }

    @Test
    public void testIndexWithoutTestItems() {
        String launchId = "2";
        when(launchRepository.findEntryById(eq(launchId))).thenReturn(createLaunch(launchId));
        logIndexerService.indexLogs(launchId, Collections.emptyList());
        verifyZeroInteractions(mongoOperations, testItemRepository, logRepository, analyzerServiceClient);
    }

    @Test
    public void testIndexTestItemsWithoutLogs() {
        String launchId = "3";
        when(launchRepository.findEntryById(eq(launchId))).thenReturn(createLaunch(launchId));
        when(logRepository.findByTestItemRef(anyString())).thenReturn(Collections.emptyList());
        int testItemCount = 10;
        logIndexerService.indexLogs(launchId, createTestItems(testItemCount));
        verify(logRepository, times(testItemCount)).findByTestItemRef(anyString());
        verifyZeroInteractions(mongoOperations, analyzerServiceClient);
    }

    @Test
    public void testIndex() {
        String launchId = "4";
        when(launchRepository.findEntryById(eq(launchId))).thenReturn(createLaunch(launchId));
        when(logRepository.findByTestItemRef(anyString())).thenReturn(Collections.singletonList(new Log()));
        int testItemCount = 2;
        when(analyzerServiceClient.index(anyListOf(IndexLaunch.class))).thenReturn(createIndexRs(testItemCount));
        logIndexerService.indexLogs(launchId, createTestItems(testItemCount));
        verify(logRepository, times(testItemCount)).findByTestItemRef(anyString());
        verify(analyzerServiceClient).index(anyListOf(IndexLaunch.class));
    }

    @Test
    public void testIndexAllLogsWithoutLogs() {
        DBCollection checkpointColl = mock(DBCollection.class);
        when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
        when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
        when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
        logIndexerService.indexAllLogs();
        verifyZeroInteractions(launchRepository, testItemRepository, analyzerServiceClient);
    }

    @Test
    public void testIndexAllLogsWithoutLaunches() {
        DBCollection checkpointColl = mock(DBCollection.class);
        when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
        when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
        when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
        when(launchRepository.findOne(anyString())).thenReturn(null);
        logIndexerService.indexAllLogs();
        verifyZeroInteractions(testItemRepository, analyzerServiceClient);
    }

    @Test
    public void testIndexAllLogsWithoutTestItems() {
        DBCollection checkpointColl = mock(DBCollection.class);
        when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
        when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
        when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(0));
        when(launchRepository.findOne(anyString())).thenReturn(createLaunch("launchId"));
        when(testItemRepository.findOne(anyString())).thenReturn(createTestItem("testItemId"));
        logIndexerService.indexAllLogs();
        verifyZeroInteractions(analyzerServiceClient);
    }

    @Test
    public void testIndexAllLogs() {
        DBCollection checkpointColl = mock(DBCollection.class);
        when(mongoOperations.getCollection(eq("logIndexingCheckpoint"))).thenReturn(checkpointColl);
        when(checkpointColl.findOne(any(Query.class))).thenReturn(null);
        int batchCount = 5;
        int logCount = batchCount * 1000;
        when(mongoOperations.stream(any(Query.class), eq(Log.class))).thenReturn(createLogIterator(logCount));
        when(launchRepository.findOne(anyString())).thenReturn(createLaunch("launchId"));
        when(testItemRepository.findOne(anyString())).thenReturn(createTestItem("testItemId"));
        logIndexerService.indexAllLogs();
        verify(checkpointColl, times(batchCount)).save(any(DBObject.class));
        verify(analyzerServiceClient, times(batchCount)).index(anyListOf(IndexLaunch.class));
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
        return ti;
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
            rsItem.setCreated(true);
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
                l.setTestItemRef("testItem" + id);
                return l;
            }
        };
    }

}
