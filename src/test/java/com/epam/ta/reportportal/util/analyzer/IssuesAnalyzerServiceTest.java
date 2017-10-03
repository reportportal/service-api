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
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.util.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.util.analyzer.model.IndexTestItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link IssuesAnalyzerService}
 *
 * @author Ivan Sharamet
 */
public class IssuesAnalyzerServiceTest {

	@Mock
	private AnalyzerServiceClient analyzerServiceClient;
	@Mock
	private LaunchRepository launchRepository;
	@Mock
	private LogRepository logRepository;

	@InjectMocks
	private IssuesAnalyzerService analyzerService;

	@Before
	public void setup() {
		analyzerService = new IssuesAnalyzerService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAnalyzeWithNonExistentLaunchId() {
		String launchId = "1";
		when(launchRepository.findEntryById(Mockito.eq(launchId))).thenReturn(null);
		analyzerService.analyze(launchId, createTestItems(1));
		verifyZeroInteractions(logRepository, analyzerServiceClient);
	}

	@Test
	public void testAnalyzeWithoutTestItems() {
		String launchId = "2";
		when(launchRepository.findEntryById(Mockito.eq(launchId))).thenReturn(createLaunch(launchId));
		analyzerService.analyze(launchId, Collections.emptyList());
		verifyZeroInteractions(logRepository, analyzerServiceClient);
	}

	@Test
	public void testAnalyzeTestItemsWithoutLogs() {
		String launchId = "3";
		when(launchRepository.findOne(Mockito.eq(launchId))).thenReturn(createLaunch(launchId));
		when(logRepository.findByTestItemRef(Mockito.anyString())).thenReturn(Collections.emptyList());
		int testItemCount = 10;
		analyzerService.analyze(launchId, createTestItems(testItemCount));
		verify(logRepository, Mockito.times(testItemCount)).findByTestItemRef(Mockito.anyString());
		verifyZeroInteractions(analyzerServiceClient);
	}

	@Test
	public void testAnalyze() {
		String launchId = "4";
		when(launchRepository.findOne(Mockito.eq(launchId))).thenReturn(createLaunch(launchId));
		when(logRepository.findByTestItemRef(Mockito.anyString())).thenReturn(Collections.singletonList(new Log()));
		int testItemCount = 2;
		when(analyzerServiceClient.analyze(Mockito.any(IndexLaunch.class))).thenReturn(crateAnalyzeRs(testItemCount));
		List<TestItem> rs = analyzerService.analyze(launchId, createTestItems(testItemCount));
		rs.forEach(ti -> Assert.assertEquals("ISSUE" + ti.getId(), ti.getIssue().getIssueType()));
		verify(logRepository, Mockito.times(testItemCount)).findByTestItemRef(Mockito.anyString());
		verify(analyzerServiceClient).analyze(Mockito.any(IndexLaunch.class));
		verifyZeroInteractions(analyzerServiceClient);
	}

	private Launch createLaunch(String id) {
		Launch l = new Launch();
		l.setId(id);
		l.setName("launch" + id);
		return l;
	}

	private List<TestItem> createTestItems(int count) {
		List<TestItem> testItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TestItem ti = new TestItem();
			ti.setId(String.valueOf(i));
			testItems.add(ti);
		}
		return testItems;
	}

	private IndexLaunch crateAnalyzeRs(int count) {
		IndexLaunch rs = new IndexLaunch();
		List<IndexTestItem> rsTestItems = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			IndexTestItem ti = new IndexTestItem();
			ti.setTestItemId(String.valueOf(i));
			ti.setIssueType("ISSUE" + i);
			rsTestItems.add(ti);
		}
		rs.setTestItems(rsTestItems);
		return rs;
	}
}
