/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.database.entity.Status.FAILED;
import static com.epam.ta.reportportal.database.entity.Status.SKIPPED;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.PRODUCT_BUG;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.epam.ta.reportportal.database.dao.FailReferenceResourceRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.database.entity.item.FailReferenceResource;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.builders.FailReferenceResourceBuilder;
import com.epam.ta.reportportal.ws.model.issue.Issue;

public class FinishTestItemHandlerImplTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private FinishTestItemHandlerImpl finishTestItemHandler = new FinishTestItemHandlerImpl();

	@Test
	public void verifyIssueTestEmptyIssueType() {
		thrown.expect(ReportPortalException.class);
		thrown.expectMessage(
				"Invalid test item issue type definition 'null' is requested for item 'itemId'. Valid issue types are: [NOT_ISSUE, PRODUCT_BUG, AUTOMATION_BUG, SYSTEM_ISSUE, TO_INVESTIGATE, NO_DEFECT]");
		finishTestItemHandler.verifyIssue("itemId", new Issue(), new ProjectSettings());
	}

	@Test
	public void verifyCustomIssueType() {
		String locator = "PB002";
		ProjectSettings projectSettings = new ProjectSettings();
		Map<TestItemIssueType, List<StatisticSubType>> subTypes = projectSettings.getSubTypes();
		List<StatisticSubType> statisticSubTypes = new ArrayList<>(subTypes.get(PRODUCT_BUG));
		statisticSubTypes.add(new StatisticSubType(locator, PRODUCT_BUG.getValue(), "NEW PRODUCT BUG", "NPB", "green"));
		subTypes.put(PRODUCT_BUG, statisticSubTypes);
		Issue issue = new Issue();
		issue.setIssueType(locator);
		TestItem item = new TestItem();
		item.setStatus(FAILED);
		TestItem testItem = finishTestItemHandler.awareTestItemIssueTypeFromStatus(item, issue, projectSettings, new Project());
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
		finishTestItemHandler.verifyIssue("itemId", issue, new ProjectSettings());
	}

	@Test
	public void awareTestItemIssueTypeSkippedNotIssue() {
		TestItem testItem = new TestItem();
		testItem.setStatus(SKIPPED);
		Issue providedIssue = new Issue();
		providedIssue.setIssueType("not_Issue");

		TestItem updated = finishTestItemHandler.awareTestItemIssueTypeFromStatus(testItem, providedIssue, new ProjectSettings(),
				new Project());
		TestItemIssue issue = updated.getIssue();
		Assert.assertNull(issue);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void failedWithoutIssue() {
		String launchRef = "launchRef";
		LazyReference lazyReference = mock(LazyReference.class);
		when(lazyReference.get()).thenReturn(new FailReferenceResourceBuilder());
		finishTestItemHandler.setFailReferenceResourceBuilder(lazyReference);
		FailReferenceResourceRepository referenceResourceRepository = mock(FailReferenceResourceRepository.class);
		finishTestItemHandler.setFailReferenceResourceRepository(referenceResourceRepository);
		LaunchRepository launchRepository = mock(LaunchRepository.class);
		when(launchRepository.findOne(launchRef)).thenReturn(new Launch());
		finishTestItemHandler.setLaunchRepository(launchRepository);
		TestItem testItem = new TestItem();
		testItem.setLaunchRef(launchRef);
		testItem.setStatus(FAILED);
		Project project = new Project();
		Project.Configuration configuration = new Project.Configuration();
		configuration.setIsAutoAnalyzerEnabled(true);
		project.setConfiguration(configuration);

		TestItem updatedTestItem = finishTestItemHandler.awareTestItemIssueTypeFromStatus(testItem, null, new ProjectSettings(), project);

		Assert.assertNotNull(updatedTestItem);
		Assert.assertNotNull(updatedTestItem.getIssue());
		Assert.assertNull(updatedTestItem.getIssue().getIssueDescription());
		Assert.assertNull(updatedTestItem.getIssue().getExternalSystemIssues());
		Assert.assertEquals("TI001", updatedTestItem.getIssue().getIssueType());
		verify(referenceResourceRepository, times(1)).save(any(FailReferenceResource.class));
		verify(launchRepository, times(1)).findOne(launchRef);
	}
}