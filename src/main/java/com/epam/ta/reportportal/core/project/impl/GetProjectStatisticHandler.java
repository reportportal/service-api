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

package com.epam.ta.reportportal.core.project.impl;

import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.database.search.Condition.*;
import static com.epam.ta.reportportal.events.handler.ExternalSystemActivityHandler.UPDATE;
import static com.epam.ta.reportportal.events.handler.LaunchActivityHandler.*;
import static com.epam.ta.reportportal.events.handler.ProjectActivityHandler.UPDATE_PROJECT;
import static com.epam.ta.reportportal.events.handler.TicketActivitySubscriber.POST_ISSUE;
import static com.epam.ta.reportportal.events.handler.UserActivityHandler.CREATE_USER;
import static com.epam.ta.reportportal.events.handler.WidgetActivityEventHandler.SHARE;
import static com.epam.ta.reportportal.events.handler.WidgetActivityEventHandler.UNSHARE;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import com.epam.ta.reportportal.ws.model.Page;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.core.project.IGetProjectInfoHandler;
import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.project.info.InfoInterval;
import com.epam.ta.reportportal.database.entity.project.info.ProjectInfoWidget;
import com.epam.ta.reportportal.database.entity.statistics.ExecutionCounter;
import com.epam.ta.reportportal.database.entity.statistics.IssueCounter;
import com.epam.ta.reportportal.database.entity.user.User;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.database.search.FilterCondition;
import com.epam.ta.reportportal.events.handler.ExternalSystemActivityHandler;
import com.epam.ta.reportportal.ws.converter.ProjectInfoResourceAssembler;
import com.epam.ta.reportportal.ws.model.project.LaunchesPerUser;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Lists;

/**
 * Get project information for administrator page
 * 
 * @author Dzmitry_Kavalets
 * @author Andrei_Ramanchuk
 */
@Service
public class GetProjectStatisticHandler implements IGetProjectInfoHandler {
	private DecimalFormat formatter = new DecimalFormat("###.##");
	private static final Double WEEKS_IN_MONTH = 4.4;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository itemRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private ProjectInfoResourceAssembler projectInfoResourceAssembler;

	@Autowired
	private ProjectInfoWidgetDataConverter dataConverter;

	@Override
	public Iterable<ProjectInfoResource> getAllProjectsInfo(Filter filter, Pageable pageable) {
		final Page<ProjectInfoResource> preAssembled = projectInfoResourceAssembler
				.toPagedResources(projectRepository.findByFilter(filter, pageable));
		for (ProjectInfoResource project : preAssembled) {
			final Optional<Launch> lastLaunch = launchRepository.findLastLaunch(project.getProjectId(), DEFAULT.name());
			lastLaunch.ifPresent(launch -> project.setLastRun(launch.getStartTime()));
			project.setLaunchesQuantity(launchRepository.findLaunchesQuantity(project.getProjectId(), DEFAULT.name(), null));
		}
		return preAssembled;
	}

	@Override
	public ProjectInfoResource getProjectInfo(String projectId, String dataInterval) {
		Project project = projectRepository.findOne(projectId);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectId);

		InfoInterval interval = InfoInterval.findByName(dataInterval);
		expect(interval, notNull()).verify(BAD_REQUEST_ERROR, dataInterval);
		Date date = getStartIntervalDate(interval);

		ProjectInfoResource projectInfoResource = projectInfoResourceAssembler.toResource(project);
		projectInfoResource.setLaunchesQuantity(launchRepository.findLaunchesQuantity(project.getId(), DEFAULT.name(), date));

		// Extended information
		Map<String, Integer> map = launchRepository.findGroupedLaunchesByOwner(projectId, DEFAULT.name(), date);
		List<LaunchesPerUser> launches = this.getLaunchesInfo(map);
		projectInfoResource.setLaunchesPerUser(launches);
		projectInfoResource.setUsersQuantity(project.getUsers().size());

		List<Launch> allLaunches = getLaunchesForProjectInformation(projectId, interval);
		List<String> tickets = itemRepository.getUniqueTicketsCount(allLaunches);
		projectInfoResource.setUniqueTickets(tickets.size());
		if ((null != allLaunches) && !allLaunches.isEmpty()) {
			formatter.setRoundingMode(RoundingMode.HALF_UP);
			double value = allLaunches.size() / (interval.getCount() * WEEKS_IN_MONTH);
			projectInfoResource.setLaunchesPerWeek(formatter.format(value));
		} else {
			projectInfoResource.setLaunchesPerWeek(formatter.format(0));
		}
		return projectInfoResource;
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public Map<String, List<ChartObject>> getProjectInfoWidgetContent(String projectId, String dataInterval, String widgetCode) {
		Project project = projectRepository.findOne(projectId);
		expect(project, notNull()).verify(PROJECT_NOT_FOUND, projectId);

		InfoInterval interval = InfoInterval.findByName(dataInterval);
		expect(interval, notNull()).verify(BAD_REQUEST_ERROR, dataInterval);

		ProjectInfoWidget widgetType = ProjectInfoWidget.findByCode(widgetCode);
		expect(widgetType, notNull()).verify(BAD_REQUEST_ERROR, widgetCode);

		Map<String, List<ChartObject>> result = new HashMap<>();
		List<Launch> allLaunches = getLaunchesForProjectInformation(projectId, interval);
		switch (widgetType) {
		case INVESTIGATED:
			result = dataConverter.getInvestigatedProjectInfo(allLaunches, interval);
			break;
		case CASES_STATISTIC:
			result = dataConverter.getTestCasesStatisticsProjectInfo(allLaunches);
			break;
		case LAUNCHES_QUANTITY:
			result = dataConverter.getLaunchesQuantity(allLaunches, interval);
			break;
		case ISSUES_CHART:
			result = dataConverter.getLaunchesIssues(allLaunches, interval);
			break;
		case ACTIVITIES:
			result = getActivities(projectId, interval);
			break;
		case LAST_LAUNCH:
			result = getLastLaunchStatistics(projectId);
			break;
		default:
			// do nothing
		}
		return result;
	}

	@SuppressWarnings("serial")
	private Map<String, List<ChartObject>> getActivities(String projectId, InfoInterval interval) {
		String value = UPDATE_PROJECT + "," + START + "," + FINISH + "," + DELETE + "," + SHARE + "," + UNSHARE + "," + POST_ISSUE + ","
				+ CREATE_USER + "," + UPDATE + "," + ExternalSystemActivityHandler.CREATE + "," + ExternalSystemActivityHandler.DELETE;
		int limit = 150;
		Filter filter = new Filter(Activity.class, new HashSet<FilterCondition>() {
			{
				add(new FilterCondition(IN, false, value, "actionType"));
				add(new FilterCondition(EQUALS, false, projectId, "projectRef"));
				add(new FilterCondition(GREATER_THAN_OR_EQUALS, false, String.valueOf(getStartIntervalDate(interval).getTime()),
						"last_modified"));
			}
		});
		List<Activity> activities = activityRepository.findByFilterWithSortingAndLimit(filter,
				new Sort(new Sort.Order(DESC, "last_modified")), limit);
		List<ChartObject> chartObjects = activities.stream().map(it -> {
			ChartObject chartObject = new ChartObject();
			chartObject.setId(it.getId());
			HashMap<String, String> values = new HashMap<>();
			values.put("actionType", it.getActionType());
			values.put("last_modified", String.valueOf(it.getLastModified().getTime()));
			values.put("objectType", it.getObjectType());
			values.put("projectRef", it.getProjectRef());
			values.put("userRef", it.getUserRef());
			if (it.getLoggedObjectRef() != null)
				values.put("loggedObjectRef", it.getLoggedObjectRef());
			if (it.getName() != null) {
				values.put("name", it.getName());
			}
			it.getHistory().entrySet().stream().forEach(entry -> {
				Activity.FieldValues fieldValues = entry.getValue();
				values.put(entry.getKey() + "$oldValue", fieldValues == null ? null : entry.getValue().getOldValue());
				values.put(entry.getKey() + "$newValue", fieldValues == null ? null : entry.getValue().getNewValue());
			});
			chartObject.setValues(values);
			return chartObject;
		}).collect(toList());

		return new HashMap<String, List<ChartObject>>() {
			{
				put("result", chartObjects);
			}
		};
	}

	@SuppressWarnings("serial")
	private Map<String, List<ChartObject>> getLastLaunchStatistics(String projectId) {
		String total = "statistics$executionCounter$total";
		String productBug = "statistics$issueCounter$productBug";
		String toInvestigate = "statistics$issueCounter$toInvestigate";
		String systemIssue = "statistics$issueCounter$systemIssue";
		String automationBug = "statistics$issueCounter$automationBug";
		String failed = "statistics$executionCounter$failed";
		String passed = "statistics$executionCounter$passed";
		String skipped = "statistics$executionCounter$skipped";
		Optional<Launch> launchOptional = launchRepository.findLastLaunch(projectId, DEFAULT.name());

		if (!launchOptional.isPresent()) {
			return new HashMap<>();
		}
		Launch lastLaunch = launchOptional.get();
		ChartObject chartObject = new ChartObject();
		ExecutionCounter executionCounter = lastLaunch.getStatistics().getExecutionCounter();
		IssueCounter issueCounter = lastLaunch.getStatistics().getIssueCounter();
		chartObject.setValues(new HashMap<String, String>() {
			{
				put(failed, executionCounter.getFailed().toString());
				put(passed, executionCounter.getPassed().toString());
				put(skipped, executionCounter.getSkipped().toString());
				put(total, executionCounter.getTotal().toString());
				put(productBug, issueCounter.getProductBugTotal().toString());
				put(toInvestigate, issueCounter.getToInvestigateTotal().toString());
				put(systemIssue, issueCounter.getSystemIssueTotal().toString());
				put(automationBug, issueCounter.getAutomationBugTotal().toString());
			}
		});
		chartObject.setId(lastLaunch.getId());
		chartObject.setName(lastLaunch.getName());
		chartObject.setStartTime(String.valueOf(lastLaunch.getStartTime().getTime()));
		chartObject.setNumber(lastLaunch.getNumber().toString());
		return new HashMap<String, List<ChartObject>>() {
			{
				put("result", Collections.singletonList(chartObject));
			}
		};
	}

	/**
	 * Utility method for extending launches information with user full name
	 * 
	 * @param input
	 * @return
	 */
	private List<LaunchesPerUser> getLaunchesInfo(Map<String, Integer> input) {
		List<LaunchesPerUser> result = Lists.newArrayList();
		Iterator<Entry<String, Integer>> iterator = input.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Integer> pair = iterator.next();
			User user = userRepository.findOne(pair.getKey());
			if (null != user) {
				LaunchesPerUser single = new LaunchesPerUser(user.getFullName(), input.get(pair.getKey()));
				result.add(single);
			}
			iterator.remove();
		}
		return result;
	}

	private List<Launch> getLaunchesForProjectInformation(String projectId, InfoInterval interval) {
		Date date = getStartIntervalDate(interval);
		return launchRepository.findLaunchesByProjectId(projectId, date, DEFAULT.name());
	}

	/**
	 * Utility method for calculation of start interval date
	 * 
	 * @param input
	 * @return
	 */
	private static Date getStartIntervalDate(InfoInterval input) {
		DateTime now = new DateTime().toDateTime(DateTimeZone.UTC);
		DateTime range = now.minusMonths(input.getCount());
		return range.toDate();
	}
}
