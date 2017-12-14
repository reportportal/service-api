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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.core.statistics.StepBasedStatisticsFacade;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Dzmitry_Kavalets
 */
public class FinishItemTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void finishTestItemBeforeStart() {
		final String launchId = "launchId";
		final String testItemId = "testItemId";
		final String owner = "owner";
		Date date = new Date();
		final LaunchRepository launchRepository = mock(LaunchRepository.class);
		final Launch launch = new Launch();
		launch.setId(launchId);
		launch.setUserRef(owner);
		when(launchRepository.findOne(launchId)).thenReturn(launch);
		final TestItemRepository testItemRepository = mock(TestItemRepository.class);
		final TestItem testItem = new TestItem();
		testItem.setId(testItemId);
		testItem.setLaunchRef(launchId);
		testItem.setStartTime(new Date(date.getTime() + 1));
		when(testItemRepository.findOne(testItemId)).thenReturn(testItem);
		final FinishTestItemRQ finishExecutionRQ = new FinishTestItemRQ();
		finishExecutionRQ.setEndTime(date);
		finishExecutionRQ.setStatus(Status.PASSED.name());

		final FinishTestItemHandlerImpl finishTestItemHandler = new FinishTestItemHandlerImpl();
		finishTestItemHandler.setTestItemRepository(testItemRepository);

		exception.expect(ReportPortalException.class);
		exception.expectMessage(
				Suppliers.formattedSupplier(ErrorType.FINISH_TIME_EARLIER_THAN_START_TIME.getDescription(), finishExecutionRQ.getEndTime(),
						date, testItemId
				).get());

		finishTestItemHandler.finishTestItem(testItemId, finishExecutionRQ, owner);
	}

	@Test
	@SuppressWarnings("serial")
	public void finishTestItem() {
		final String launchId = "launchId";
		final String owner = "owner";
		final String projectId = "projectId";
		String testItemId = "testItemId";
		final Date time = new Date();

		final ProjectRepository projectRepository = mock(ProjectRepository.class);
		final Project project = new Project();
		project.setName(projectId);
		when(projectRepository.findOne(projectId)).thenReturn(project);

		Map<TestItemIssueType, List<StatisticSubType>> types = new HashMap<TestItemIssueType, List<StatisticSubType>>() {
			{
				put(TestItemIssueType.AUTOMATION_BUG, Lists.newArrayList(
						new StatisticSubType(TestItemIssueType.AUTOMATION_BUG.getLocator(), TestItemIssueType.AUTOMATION_BUG.getValue(),
								"Automation Bug", "AB", "#f5d752"
						)));
				put(TestItemIssueType.PRODUCT_BUG, Lists.newArrayList(
						new StatisticSubType(TestItemIssueType.PRODUCT_BUG.getLocator(), TestItemIssueType.PRODUCT_BUG.getValue(),
								"Product Bug", "PB", "#e93416"
						)));
				put(TestItemIssueType.SYSTEM_ISSUE, Lists.newArrayList(
						new StatisticSubType(TestItemIssueType.SYSTEM_ISSUE.getLocator(), TestItemIssueType.SYSTEM_ISSUE.getValue(),
								"System Issue", "SI", "#2273cd"
						)));
				put(TestItemIssueType.NO_DEFECT, Lists.newArrayList(
						new StatisticSubType(TestItemIssueType.NO_DEFECT.getLocator(), TestItemIssueType.NO_DEFECT.getValue(), "No Defect",
								"ND", "#777"
						)));
				put(TestItemIssueType.TO_INVESTIGATE, Lists.newArrayList(
						new StatisticSubType(TestItemIssueType.TO_INVESTIGATE.getLocator(), TestItemIssueType.TO_INVESTIGATE.getValue(),
								"To Investigate", "TI", "#ffa500"
						)));
			}
		};

		final LaunchRepository launchRepository = mock(LaunchRepository.class);
		final Launch launch = new Launch();
		launch.setId(launchId);
		launch.setProjectRef(projectId);
		launch.setUserRef(owner);
		when(launchRepository.findOne(launchId)).thenReturn(launch);

		final TestItemRepository testItemRepository = mock(TestItemRepository.class);
		final TestItem testItem = new TestItem();
		testItem.setId(testItemId);
		testItem.setLaunchRef(launchId);
		testItem.setStartTime(time);
		when(testItemRepository.findOne(testItemId)).thenReturn(testItem);
		when(testItemRepository.hasDescendants(testItemId)).thenReturn(true);

		final FinishTestItemHandlerImpl finishTestItemHandler = new FinishTestItemHandlerImpl();
		finishTestItemHandler.setProjectRepository(projectRepository);
		finishTestItemHandler.setLaunchRepository(launchRepository);
		finishTestItemHandler.setTestItemRepository(testItemRepository);

		StatisticsFacadeFactory facadeFactoryMock = mock(StatisticsFacadeFactory.class);
		StepBasedStatisticsFacade facadeMock = mock(StepBasedStatisticsFacade.class);
		when(facadeMock.updateExecutionStatistics(any())).thenReturn(testItem);

		when(facadeFactoryMock.getStatisticsFacade(any())).thenReturn(facadeMock);
		finishTestItemHandler.setStatisticsFacadeFactory(facadeFactoryMock);

		final FinishTestItemRQ finishExecutionRQ = new FinishTestItemRQ();
		finishExecutionRQ.setStatus(Status.PASSED.name());
		finishExecutionRQ.setEndTime(time);

		final OperationCompletionRS operationCompletionRS = finishTestItemHandler.finishTestItem(testItemId, finishExecutionRQ, owner);

		String expected = "TestItem with ID = '" + testItemId + "' successfully finished.";
		Assert.assertNotNull(operationCompletionRS);
		Assert.assertEquals(expected, operationCompletionRS.getResultMessage());
	}
}