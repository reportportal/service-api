/*
 * Copyright 2016 EPAM Systems
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

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.core.launch.IRetriesLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacade;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.StatisticsCalculationStrategy;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Andrey_Ivanov1 on 01-Jun-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class InterruptBrokenLaunchesJobTest {

	@InjectMocks
	private InterruptBrokenLaunchesJob interruptBrokenLaunchesJob = new InterruptBrokenLaunchesJob();
	@Mock
	private LaunchRepository launchRepository;
	@Mock
	private TestItemRepository testItemRepository;
	@Mock
	private LogRepository logRepository;
	@Mock
	private StatisticsFacadeFactory statisticsFacadeFactory;
	@Mock
	private StatisticsFacade statisticsFacade;
	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private IRetriesLaunchHandler retriesLaunchHandler;

	private final String NAME = "name";
	private final Project PROJECT = new Project();
	private final List<Launch> LIST_OF_LAUNCHS = new ArrayList<>();
	private final List<TestItem> LIST_OF_TESTITEMS = new ArrayList<>();

	@Before
	public void setUp() {
		/*
         *  setUp PROJECT
         */
		Project.Configuration configuration = new Project.Configuration();
		configuration.setInterruptJobTime("1 day");
		configuration.setStatisticsCalculationStrategy(StatisticsCalculationStrategy.STEP_BASED);
		PROJECT.setName(NAME);
		PROJECT.setConfiguration(configuration);
		Stream<Project> sp = Stream.of(PROJECT);

        /*
         *  setUp LIST_OF_LAUNCHS with 2 Launch
         */
		Launch launch1 = new Launch();
		launch1.setId(NAME);
		launch1.setStatus(Status.IN_PROGRESS);
		launch1.setName(NAME);
		launch1.setProjectRef(NAME);

		Launch launch2 = new Launch();
		launch2.setId(NAME);
		launch2.setStatus(Status.PASSED);
		launch2.setName(NAME);
		launch2.setProjectRef(NAME);

		LIST_OF_LAUNCHS.add(launch1);
		LIST_OF_LAUNCHS.add(launch2);

        /*
         *  setUp LIST_OF_TESTITEMS with 2 TestItems
         */
		ExecutionCounter exCounter = new ExecutionCounter();
		exCounter.setFailed(1);
		exCounter.setPassed(2);
		exCounter.setSkipped(3);
		exCounter.setTotal(6);
		IssueCounter issCounter = new IssueCounter();
		issCounter.setAutomationBug(NAME, 1);
		issCounter.setNoDefect(NAME, 1);
		issCounter.setProductBug(NAME, 1);
		issCounter.setSystemIssue(NAME, 1);
		issCounter.setToInvestigate(NAME, 1);
		Statistics stat = new Statistics(exCounter, issCounter);

		TestItemIssue testItemIssue = new TestItemIssue();
		testItemIssue.setIssueDescription(NAME);
		testItemIssue.setIssueType(NAME);

		TestItem testItem1 = new TestItem();
		testItem1.setId(NAME);
		testItem1.setHasChilds(false);
		testItem1.setStatus(Status.IN_PROGRESS);
		testItem1.setStatistics(stat);
		testItem1.setIssue(testItemIssue);
		testItem1.setParent(NAME);
		TestItem testItem2 = new TestItem();
		testItem2.setId(NAME);
		testItem2.setHasChilds(true);
		testItem2.setStatus(Status.PASSED);
		testItem2.setStatistics(stat);
		testItem2.setIssue(testItemIssue);
		testItem2.setParent(NAME);

		LIST_OF_TESTITEMS.add(testItem1);
		LIST_OF_TESTITEMS.add(testItem2);

		//common preconditions
		when(projectRepository.streamAllIdsAndConfiguration()).thenReturn(sp);
		when(!launchRepository.hasItems(eq(LIST_OF_LAUNCHS.get(1)), eq(Status.IN_PROGRESS))).thenReturn(true);
		when(launchRepository.findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), any(String.class))).thenReturn(
				LIST_OF_LAUNCHS);
		when(testItemRepository.findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), eq(LIST_OF_LAUNCHS.get(1)))).thenReturn(
				LIST_OF_TESTITEMS);
	}

	@Test
	public void runTestWithItemsInLaunchAndTestItemRepo() {
		//test preconditions
		when(testItemRepository.hasLogs(eq(LIST_OF_TESTITEMS))).thenReturn(true);
		when(logRepository.hasLogsAddedLately(any(Duration.class), eq(LIST_OF_TESTITEMS.get(1)))).thenReturn(true);
		//run
		interruptBrokenLaunchesJob.execute(null);
		//verifies
		verify(projectRepository, times(1)).streamAllIdsAndConfiguration();
		verify(launchRepository, times(1)).findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), any(String.class));
		verify(launchRepository, times(2)).hasItems(any(Launch.class), eq(Status.IN_PROGRESS));
		verify(testItemRepository, times(1)).hasTestItemsAddedLately(any(Duration.class), any(Launch.class), eq(Status.IN_PROGRESS));
		verify(testItemRepository, times(1)).findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), any(Launch.class));
		verify(testItemRepository, times(1)).hasLogs(LIST_OF_TESTITEMS);
		verify(logRepository, times(2)).hasLogsAddedLately(any(Duration.class), any(TestItem.class));
		verify(launchRepository, times(1)).save(any(Launch.class));
	}

	@Test
	public void runTestWithBrokenLaunch() {
		//test preconditions
		when(testItemRepository.hasLogs(eq(LIST_OF_TESTITEMS))).thenReturn(true);
		when(testItemRepository.findInStatusItems(anyString(), anyString())).thenReturn(LIST_OF_TESTITEMS);
		when(testItemRepository.save(any(TestItem.class))).thenReturn(LIST_OF_TESTITEMS.get(0));
		when(launchRepository.findOne(anyString())).thenReturn(LIST_OF_LAUNCHS.get(0));
		when(testItemRepository.findOne(anyString())).thenReturn(LIST_OF_TESTITEMS.get(1));
		when(projectRepository.findOne(anyString())).thenReturn(PROJECT);
		when(statisticsFacadeFactory.getStatisticsFacade(any(StatisticsCalculationStrategy.class))).thenReturn(statisticsFacade);
		when(statisticsFacade.updateExecutionStatistics(LIST_OF_TESTITEMS.get(0))).thenReturn(LIST_OF_TESTITEMS.get(0));
		when(statisticsFacade.updateIssueStatistics(LIST_OF_TESTITEMS.get(0))).thenReturn(LIST_OF_TESTITEMS.get(0));
		//run
		interruptBrokenLaunchesJob.execute(null);
		//verifies
		verify(projectRepository, times(1)).streamAllIdsAndConfiguration();
		verify(launchRepository, times(1)).findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), any(String.class));
		verify(launchRepository, times(2)).hasItems(any(Launch.class), eq(Status.IN_PROGRESS));
		verify(testItemRepository, times(1)).hasTestItemsAddedLately(any(Duration.class), any(Launch.class), eq(Status.IN_PROGRESS));
		verify(testItemRepository, times(1)).findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), any(Launch.class));
		verify(testItemRepository, times(1)).hasLogs(LIST_OF_TESTITEMS);
		verify(logRepository, times(2)).hasLogsAddedLately(any(Duration.class), any(TestItem.class));
		verify(testItemRepository, times(1)).findInStatusItems(eq(Status.IN_PROGRESS.name()), anyString());
		verify(launchRepository, times(2)).save(any(Launch.class));
		verify(launchRepository, times(1)).findOne(anyString());
		verify(testItemRepository, times(2)).save(any(TestItem.class));
		verify(projectRepository, times(2)).findOne(anyString());
		verify(statisticsFacade, times(2)).updateExecutionStatistics(any(TestItem.class));
		verify(statisticsFacade, times(2)).updateIssueStatistics(any(TestItem.class));
		verify(testItemRepository, times(2)).findOne(anyString());
	}

	@Test
	public void runTestWithoutLogsInTestItemsRepo() {
		//run
		interruptBrokenLaunchesJob.execute(null);
		//verifies
		verify(projectRepository, times(1)).streamAllIdsAndConfiguration();
		verify(launchRepository, times(1)).findModifiedLaterAgo(any(Duration.class), eq(Status.IN_PROGRESS), any(String.class));
		verify(launchRepository, times(1)).save(any(Launch.class));
	}

}
