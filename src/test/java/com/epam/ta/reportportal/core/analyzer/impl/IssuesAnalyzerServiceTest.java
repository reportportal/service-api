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

import com.epam.ta.reportportal.core.analyzer.IAnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.ILogIndexer;
import com.epam.ta.reportportal.core.analyzer.model.AnalyzedItemRs;
import com.epam.ta.reportportal.core.analyzer.model.IndexLaunch;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.core.statistics.StepBasedStatisticsFacade;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.*;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.PRODUCT_BUG;
import static org.mockito.Mockito.*;

/**
 * @author Pavel Bortnik
 */
public class IssuesAnalyzerServiceTest {
	@Mock
	private IAnalyzerServiceClient analyzerServiceClient;
	@Mock
	private TestItemRepository testItemRepository;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private StatisticsFacadeFactory statisticsFacadeFactory;
	@Mock
	private LogRepository logRepository;
	@Mock
	private ILogIndexer logIndexer;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private IIssuesAnalyzer issuesAnalyzer;

	@Before
	public void setup() {
		issuesAnalyzer = new IssuesAnalyzerService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void hasAnalyzers() {
		when(analyzerServiceClient.hasClients()).thenReturn(true);
		Assert.assertTrue(issuesAnalyzer.hasAnalyzers());
	}

	@Test
	public void analyzeWithoutLogs() {
		Launch launch = launch();
		TestItem testItems = testItemsTI(1).get(0);
		when(logRepository.findGreaterOrEqualLevel(testItems.getId(), LogLevel.ERROR)).thenReturn(Collections.emptyList());
		issuesAnalyzer.analyze(launch, Collections.singletonList(testItems));
		verify(logRepository, times(1)).findGreaterOrEqualLevel(testItems.getId(), LogLevel.ERROR);
		verifyZeroInteractions(analyzerServiceClient);
	}

	@Test
	public void analyze() {
		int itemsCount = 2;
		Launch launch = launch();
		Project project = project();
		List<TestItem> items = testItemsTI(itemsCount);

		when(logRepository.findGreaterOrEqualLevel(anyString(), eq(LogLevel.ERROR))).thenReturn(errorLogs(2));
		when(analyzerServiceClient.analyze(any())).thenReturn(analyzedItems(itemsCount));
		when(projectRepository.findByName(launch.getProjectRef())).thenReturn(project);

		StepBasedStatisticsFacade mock = mock(StepBasedStatisticsFacade.class);
		when(statisticsFacadeFactory.getStatisticsFacade(StatisticsCalculationStrategy.STEP_BASED)).thenReturn(mock);

		issuesAnalyzer.analyze(launch, items);

		verify(logRepository, times(itemsCount)).findGreaterOrEqualLevel(anyString(), eq(LogLevel.ERROR));
		verify(analyzerServiceClient, times(1)).analyze(any());
		verify(testItemRepository, times(1)).updateItemsIssues(any());
		verify(projectRepository, times(1)).findByName(launch.getProjectRef());
		verify(statisticsFacadeFactory, times(1)).getStatisticsFacade(StatisticsCalculationStrategy.STEP_BASED);
		verify(mock, times(1)).recalculateStatistics(launch);
		verify(logIndexer, times(1)).indexLogs(eq(launch.getId()), anyListOf(TestItem.class));
		verify(eventPublisher, times(2)).publishEvent(any(ItemIssueTypeDefined.class));
	}

	private Project project() {
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setStatisticsCalculationStrategy(StatisticsCalculationStrategy.STEP_BASED);
		project.setConfiguration(configuration);
		return project;
	}

	private Launch launch() {
		Launch launch = new Launch();
		launch.setId("launch");
		launch.setName("launch");
		launch.setProjectRef("project");
		return launch;
	}

	private List<TestItem> testItemsTI(int count) {
		List<TestItem> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TestItem test = new TestItem();
			test.setId(String.valueOf(i));
			test.setName("test" + i);
			test.setUniqueId("unique" + i);
			test.setIssue(new TestItemIssue());
			list.add(test);
		}
		return list;
	}

	private List<Log> errorLogs(int count) {
		List<Log> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			Log log = new Log();
			log.setLogMsg("Error message " + i);
			log.setLevel(LogLevel.ERROR);
			list.add(log);
		}
		return list;
	}

	private IndexLaunch analyzedLaunch(int itemsCount) {
		IndexLaunch indexLaunch = new IndexLaunch();
		indexLaunch.setLaunchId("indexLaunch");
		indexLaunch.setLaunchName("launch");
		indexLaunch.setProject("project");
		return indexLaunch;
	}

	private Set<AnalyzedItemRs> analyzedItems(int itemsCount) {
		Set<AnalyzedItemRs> list = new HashSet<>();
		for (int i = 0; i < itemsCount; i++) {
			AnalyzedItemRs testItem = new AnalyzedItemRs();
			testItem.setItemId(String.valueOf(i));
			testItem.setIssueType(PRODUCT_BUG.getLocator());
			list.add(testItem);
		}
		return list;
	}
}