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
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.project.settings.impl;

import com.epam.ta.reportportal.core.project.settings.IDeleteProjectSettingsHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.events.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.fail;
import static com.epam.ta.reportportal.core.widget.content.WidgetDataTypes.*;
import static com.epam.ta.reportportal.database.entity.item.issue.TestItemIssueType.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.stream.Collectors.toList;

/**
 * Initial realization of
 * {@link com.epam.ta.reportportal.core.project.settings.IDeleteProjectSettingsHandler}
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class DeleteProjectSettingsHandler implements IDeleteProjectSettingsHandler {

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	public void setProjectRepository(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Override
	public OperationCompletionRS deleteProjectIssueSubType(String projectName, String user, String id) {
		/* Validate project existence */
		Project project = projectRepository.findOne(projectName);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectName);

		/* Validate project settings existence */
		Project projectsClone = org.apache.commons.lang3.SerializationUtils.clone(project);

		/* Validate target issue sub-type existence */
		StatisticSubType type = project.getConfiguration().getByLocator(id);
		expect(type, notNull()).verify(ISSUE_TYPE_NOT_FOUND, id);
		StatisticSubType group = project.getConfiguration().getByLocator(type.getTypeRef());

		/* Any other BRs? */
		if (Sets.newHashSet(AUTOMATION_BUG.getLocator(), PRODUCT_BUG.getLocator(), SYSTEM_ISSUE.getLocator(), NO_DEFECT.getLocator(),
				TO_INVESTIGATE.getLocator(), IssueCounter.GROUP_TOTAL
		).contains(type.getLocator())) {

			fail().withError(FORBIDDEN_OPERATION, "You cannot remove predefined global issue types.");
		}

		project.getConfiguration()
				.getSubTypes()
				.forEach((k, v) -> project.getConfiguration()
						.getSubTypes()
						.put(k, v.stream().filter(one -> !one.getLocator().equals(id)).collect(toList())));

		List<String> ids = launchRepository.findLaunchesWithSpecificStat(projectName, type).stream().map(Launch::getId).collect(toList());

		/* Drop removing sub-type statistic from end-point elements */
		List<TestItem> items = testItemRepository.findForSpecifiedSubType(ids, false, type);
		/* parallelStream and remove sync? */
		items.forEach(testItem -> {
			/* Sync against cross-access to suites and tests */
			synchronized (this) {
				/* Update statistic only if issueType equals deleting ID!!! */
				if (testItem.getIssue() != null && id.equals(testItem.getIssue().getIssueType())) {
					testItem = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
							.resetIssueStatistics(testItem);
					TestItemIssue testItemIssue = testItem.getIssue();
					testItemIssue.setIssueType(group.getLocator());
					testItemRepository.save(testItem);
					statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
							.updateIssueStatistics(testItem);
				}
			}
		});

		/* Update launches with removing sub-type */
		ids.forEach(launch -> launchRepository.dropIssueStatisticsType(launch, type));

		/* Update testItems with removing sub-type */
		/* End-point test-items (methods) */
		items.forEach(testItem -> testItemRepository.dropIssueStatisticsType(testItem.getId(), type));

		/* Transitional test-items (Suites, tests, etc.) */
		List<TestItem> testItemsAddition = testItemRepository.findForSpecifiedSubType(ids, true, type);
		testItemsAddition.forEach(testItem -> testItemRepository.dropIssueStatisticsType(testItem.getId(), type));

		try {
			projectRepository.save(project);
			widgetRepository.findByProject(projectName)
					.stream()
					.filter(it -> {
						String widgetType = it.getContentOptions().getType();
						return widgetType.equals(LINE_CHART.getType()) || widgetType.equals(COLUMN_CHART.getType()) || widgetType.equals(
								LAUNCHES_TABLE.getType()) || widgetType.equals(TABLE.getType()) || widgetType.equals(PIE_CHART.getType())
								|| widgetType.equals(STATISTICS_PANEL.getType()) || widgetType.equals(TRENDS_CHART.getType());
					})
					.forEach(it -> widgetRepository.removeContentField(it.getId(),
							"statistics$defects$" + type.getTypeRef().toLowerCase() + "$" + id
					));
		} catch (Exception e) {
			throw new ReportPortalException("Error during project settings issue sub-type update saving.", e);
		}

		eventPublisher.publishEvent(new DefectTypeDeletedEvent(id, projectsClone, user));
		return new OperationCompletionRS("Issue sub-type delete operation completed successfully.");
	}
}