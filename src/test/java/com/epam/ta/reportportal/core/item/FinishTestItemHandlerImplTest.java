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

package com.epam.ta.reportportal.core.item;

import com.epam.ta.reportportal.core.analyzer.IIssuesAnalyzer;
import com.epam.ta.reportportal.core.analyzer.IssuesAnalyzerService;
import com.epam.ta.reportportal.database.dao.ExternalSystemRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.epam.ta.reportportal.database.entity.Status.FAILED;
import static com.epam.ta.reportportal.database.entity.Status.SKIPPED;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.PRODUCT_BUG;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;

public class FinishTestItemHandlerImplTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private FinishTestItemHandlerImpl finishTestItemHandler = new FinishTestItemHandlerImpl();

	@Test
	public void verifyIssueTestEmptyIssueType() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				"Invalid test item issue type definition 'null' is requested for item 'itemId'. Valid issue types are: [NOT_ISSUE, PRODUCT_BUG, AUTOMATION_BUG, SYSTEM_ISSUE, TO_INVESTIGATE, NO_DEFECT]");
		finishTestItemHandler.verifyIssue("itemId", new Issue(), new Project.Configuration());
	}

	@Test
	public void verifyCustomIssueType() {
		String locator = "PB002";
		final Project project = new Project();
		Project.Configuration projectSettings = new Project.Configuration();
		project.setConfiguration(projectSettings);
		Map<TestItemIssueType, List<StatisticSubType>> subTypes = projectSettings.getSubTypes();
		List<StatisticSubType> statisticSubTypes = new ArrayList<>(subTypes.get(PRODUCT_BUG));
		statisticSubTypes.add(new StatisticSubType(locator, PRODUCT_BUG.getValue(), "NEW PRODUCT BUG", "NPB", "green"));
		subTypes.put(PRODUCT_BUG, statisticSubTypes);
		Issue issue = new Issue();
		issue.setIssueType(locator);
		TestItem item = new TestItem();
		item.setStatus(FAILED);
		item.setType(TestItemType.STEP);
		TestItem testItem = finishTestItemHandler.awareTestItemIssueTypeFromStatus(item, issue, project, "someuser");
		Assert.assertNotNull(testItem);
		TestItemIssue testItemIssue = testItem.getIssue();
		Assert.assertNotNull(testItemIssue);
		Assert.assertEquals(locator, testItemIssue.getIssueType());
		Assert.assertNull(testItemIssue.getIssueDescription());
	}

	@Test
	public void incorrectIssueType() {
		Issue issue = new Issue();
		issue.setIssueType("PB004");
		thrown.expectMessage(
				"Invalid test item issue type definition 'PB004' is requested for item 'itemId'. Valid issue types are: [NOT_ISSUE, PRODUCT_BUG, AUTOMATION_BUG, SYSTEM_ISSUE, TO_INVESTIGATE, NO_DEFECT]");
		thrown.expect(ReportPortalException.class);
		finishTestItemHandler.verifyIssue("itemId", issue, new Project.Configuration());
	}

	@Test
	public void awareTestItemIssueTypeSkippedNotIssue() {
		TestItem testItem = new TestItem();
		testItem.setStatus(SKIPPED);
		testItem.setType(TestItemType.STEP);
		Issue providedIssue = new Issue();
		providedIssue.setIssueType("not_Issue");

		TestItem updated = finishTestItemHandler.awareTestItemIssueTypeFromStatus(testItem, providedIssue, new Project(), "user");
		TestItemIssue issue = updated.getIssue();
		Assert.assertNull(issue);
	}

	@Test
	@Ignore
	public void analyzeOnFinish() {
		String launchRef = "launchRef";
		LaunchRepository launchRepository = mock(LaunchRepository.class);
		IIssuesAnalyzer analyzerService = mock(IssuesAnalyzerService.class);
		Launch launch = new Launch();
		launch.setId(launchRef);
		when(launchRepository.findOne(launchRef)).thenReturn(launch);
		finishTestItemHandler.setLaunchRepository(launchRepository);
		finishTestItemHandler.setIssuesAnalyzer(analyzerService);
		TestItem testItem = new TestItem();
		testItem.setLaunchRef(launchRef);
		testItem.setStatus(FAILED);
		testItem.setType(TestItemType.STEP);
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setIsAutoAnalyzerEnabled(true);
		configuration.setAnalyzeOnTheFly(true);
		project.setConfiguration(configuration);

		TestItem analyzed = new TestItem();
		analyzed.setLaunchRef(launchRef);
		analyzed.setStatus(FAILED);
		analyzed.setIssue(new TestItemIssue(TestItemIssueType.PRODUCT_BUG.getLocator(), null));
		analyzed.setType(TestItemType.STEP);

		List<TestItem> forAnalyze = Collections.singletonList(testItem);
		doNothing().when(analyzerService).analyze(eq(launch), eq(forAnalyze));

		TestItem updatedTestItem = finishTestItemHandler.awareTestItemIssueTypeFromStatus(testItem, null, project, "someuser");

		Assert.assertNotNull(updatedTestItem);
		Assert.assertNotNull(updatedTestItem.getIssue());
		Assert.assertNull(updatedTestItem.getIssue().getIssueDescription());
		Assert.assertNull(updatedTestItem.getIssue().getExternalSystemIssues());
		Assert.assertEquals("PB001", updatedTestItem.getIssue().getIssueType());
		verify(launchRepository, times(1)).findOne(launchRef);
		verify(analyzerService, times(1)).analyze(launch, forAnalyze);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void failedWithoutIssue() {
		String launchRef = "launchRef";
		LaunchRepository launchRepository = mock(LaunchRepository.class);
		when(launchRepository.findOne(launchRef)).thenReturn(new Launch());
		finishTestItemHandler.setLaunchRepository(launchRepository);
		TestItem testItem = new TestItem();
		testItem.setLaunchRef(launchRef);
		testItem.setStatus(FAILED);
		testItem.setType(TestItemType.STEP);
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setIsAutoAnalyzerEnabled(true);
		project.setConfiguration(configuration);

		TestItem updatedTestItem = finishTestItemHandler.awareTestItemIssueTypeFromStatus(testItem, null, project, "someuser");

		Assert.assertNotNull(updatedTestItem);
		Assert.assertNotNull(updatedTestItem.getIssue());
		Assert.assertNull(updatedTestItem.getIssue().getIssueDescription());
		Assert.assertNull(updatedTestItem.getIssue().getExternalSystemIssues());
		Assert.assertEquals("TI001", updatedTestItem.getIssue().getIssueType());
	}

	@Test
	public void failedWithExternalIssue() {
		Issue issue = new Issue();
		issue.setIssueType(TestItemIssueType.AUTOMATION_BUG.getLocator());

		Issue.ExternalSystemIssue externalIssue = new Issue.ExternalSystemIssue();
		externalIssue.setExternalSystemId("mocked");
		externalIssue.setTicketId("mocked");
		externalIssue.setUrl("http://someUrl");
		issue.setExternalSystemIssues(Sets.newHashSet(externalIssue));

		TestItem item = new TestItem();
		item.setStatus(FAILED);
		item.setType(TestItemType.STEP);

		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setIsAutoAnalyzerEnabled(false);
		project.setConfiguration(configuration);

		ExternalSystemRepository externalSystemRepository = mock(ExternalSystemRepository.class);
		when(externalSystemRepository.exists(Mockito.anyString())).thenReturn(true);
		finishTestItemHandler.setExternalSystemRepository(externalSystemRepository);

		TestItem testItem = finishTestItemHandler.awareTestItemIssueTypeFromStatus(item, issue, new Project(), "someuser");
		Assert.assertNotNull(testItem);
		TestItemIssue testItemIssue = testItem.getIssue();
		Assert.assertNotNull(testItemIssue);
		Assert.assertThat(testItemIssue.getExternalSystemIssues(), not(empty()));

		Assert.assertThat(testItemIssue.getExternalSystemIssues().iterator().next().getTicketId(), Matchers.is("mocked"));
	}

	@Test
	public void failedWithExternalIssueNotPresent() {
		Issue issue = new Issue();
		issue.setIssueType(TestItemIssueType.AUTOMATION_BUG.getLocator());

		Issue.ExternalSystemIssue externalIssue = new Issue.ExternalSystemIssue();
		externalIssue.setExternalSystemId("mocked");
		externalIssue.setTicketId("mocked");
		externalIssue.setUrl("http://someUrl");
		issue.setExternalSystemIssues(Sets.newHashSet(externalIssue));

		TestItem item = new TestItem();
		item.setStatus(FAILED);
		item.setType(TestItemType.STEP);

		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setIsAutoAnalyzerEnabled(false);
		project.setConfiguration(configuration);

		ExternalSystemRepository externalSystemRepository = mock(ExternalSystemRepository.class);
		when(externalSystemRepository.exists(Mockito.anyString())).thenReturn(false);
		finishTestItemHandler.setExternalSystemRepository(externalSystemRepository);

		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(Matchers.containsString("ExternalSystem with ID 'mocked' not found"));
		finishTestItemHandler.awareTestItemIssueTypeFromStatus(item, issue, project, "someuser");
	}
}