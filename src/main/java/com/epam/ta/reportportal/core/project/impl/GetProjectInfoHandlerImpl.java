/*
 * Copyright (C) 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.core.events.activity.ActivityAction;
import com.epam.ta.reportportal.core.project.GetProjectInfoHandler;
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.email.ProjectInfoWidget;
import com.epam.ta.reportportal.entity.statistics.Statistics;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.querygen.Condition.*;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_ACTION;
import static com.epam.ta.reportportal.commons.querygen.constant.ActivityCriteriaConstant.CRITERIA_CREATION_DATE;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.core.events.activity.ActivityAction.*;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectInfoHandlerImpl implements GetProjectInfoHandler {

	private DecimalFormat formatter = new DecimalFormat("###.##");
	private static final Double WEEKS_IN_MONTH = 4.4;

	private final ProjectRepository projectRepository;

	private final LaunchRepository launchRepository;

	private final ActivityRepository activityRepository;

	private final ProjectInfoWidgetDataConverter dataConverter;

	@Autowired
	public GetProjectInfoHandlerImpl(ProjectRepository projectRepository, LaunchRepository launchRepository,
			ActivityRepository activityRepository, ProjectInfoWidgetDataConverter dataConverter) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
		this.activityRepository = activityRepository;
		this.dataConverter = dataConverter;
	}

	@Override
	public Iterable<ProjectInfoResource> getAllProjectsInfo(Filter filter, Pageable pageable) {
		return PagedResourcesAssembler.pageConverter(ProjectConverter.TO_PROJECT_INFO_RESOURCE)
				.apply(projectRepository.findProjectInfoByFilter(filter, pageable, Mode.DEFAULT.name()));
	}

	@Override
	public ProjectInfoResource getProjectInfo(ReportPortalUser.ProjectDetails projectDetails, String interval) {
		InfoInterval infoInterval = InfoInterval.findByInterval(interval)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, interval));
		ProjectInfoResource projectInfoResource = ProjectConverter.TO_PROJECT_INFO_RESOURCE.apply(projectRepository.findProjectInfoFromDate(projectDetails.getProjectId(),
				getStartIntervalDate(infoInterval),
				Mode.DEFAULT.name()
		));
		if (projectInfoResource.getLaunchesQuantity() != 0) {
			formatter.setRoundingMode(RoundingMode.HALF_UP);
			double value = projectInfoResource.getLaunchesQuantity() / (infoInterval.getCount() * WEEKS_IN_MONTH);
			projectInfoResource.setLaunchesPerWeek(formatter.format(value));
		} else {
			projectInfoResource.setLaunchesPerWeek(formatter.format(0));
		}
		return projectInfoResource;
	}

	@Override
	public Map<String, List<ChartObject>> getProjectInfoWidgetContent(ReportPortalUser.ProjectDetails projectDetails, String interval,
			String widgetCode) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectDetails.getProjectId()));
		InfoInterval infoInterval = InfoInterval.findByInterval(interval)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, interval));
		ProjectInfoWidget widgetType = ProjectInfoWidget.findByCode(widgetCode)
				.orElseThrow(() -> new ReportPortalException(BAD_REQUEST_ERROR, widgetCode));

		List<Launch> launches = launchRepository.findByProjectIdAndStartTimeGreaterThanAndMode(project.getId(),
				getStartIntervalDate(infoInterval),
				LaunchModeEnum.DEFAULT
		);

		Map<String, List<ChartObject>> result;

		switch (widgetType) {
			case INVESTIGATED:
				result = dataConverter.getInvestigatedProjectInfo(launches, infoInterval);
				break;
			case CASES_STATISTIC:
				result = dataConverter.getTestCasesStatisticsProjectInfo(launches);
				break;
			case LAUNCHES_QUANTITY:
				result = dataConverter.getLaunchesQuantity(launches, infoInterval);
				break;
			case ISSUES_CHART:
				result = dataConverter.getLaunchesIssues(launches, infoInterval);
				break;
			case ACTIVITIES:
				result = getActivities(projectDetails.getProjectId(), infoInterval);
				break;
			case LAST_LAUNCH:
				result = getLastLaunchStatistics(projectDetails.getProjectId());
				break;
			default:
				// empty result
				result = Collections.emptyMap();
		}

		return result;
	}

	private Map<String, List<ChartObject>> getLastLaunchStatistics(Long projectId) {
		Optional<Launch> launchOptional = launchRepository.findLastRun(projectId, Mode.DEFAULT.name());
		if (!launchOptional.isPresent()) {
			return Collections.emptyMap();
		}
		Launch lastLaunch = launchOptional.get();
		Set<Statistics> statistics = lastLaunch.getStatistics();

		ChartObject chartObject = new ChartObject();
		chartObject.setValues(new HashMap<String, String>() {
			{
				put(EXECUTIONS_TOTAL, getStatisticsCount(statistics, EXECUTIONS_TOTAL).orElse(0).toString());
				put(EXECUTIONS_FAILED, getStatisticsCount(statistics, EXECUTIONS_FAILED).orElse(0).toString());
				put(EXECUTIONS_PASSED, getStatisticsCount(statistics, EXECUTIONS_PASSED).orElse(0).toString());
				put(EXECUTIONS_SKIPPED, getStatisticsCount(statistics, EXECUTIONS_SKIPPED).orElse(0).toString());
				put(DEFECTS_AUTOMATION_BUG_TOTAL, getStatisticsCount(statistics, DEFECTS_PRODUCT_BUG_TOTAL).orElse(0).toString());
				put(DEFECTS_TO_INVESTIGATE_TOTAL, getStatisticsCount(statistics, DEFECTS_TO_INVESTIGATE_TOTAL).orElse(0).toString());
				put(DEFECTS_PRODUCT_BUG_TOTAL, getStatisticsCount(statistics, DEFECTS_PRODUCT_BUG_TOTAL).orElse(0).toString());
				put(DEFECTS_SYSTEM_ISSUE_TOTAL, getStatisticsCount(statistics, DEFECTS_SYSTEM_ISSUE_TOTAL).orElse(0).toString());
			}

		});
		chartObject.setName(lastLaunch.getName());
		chartObject.setId(String.valueOf(lastLaunch.getId()));
		chartObject.setNumber(String.valueOf(lastLaunch.getNumber()));
		chartObject.setStartTime(String.valueOf(Timestamp.valueOf(LocalDateTime.now()).getTime()));

		return Collections.singletonMap("result", Collections.singletonList(chartObject));
	}

	private Optional<Integer> getStatisticsCount(Set<Statistics> statistics, String param) {
		return statistics.stream()
				.filter(it -> ofNullable(it.getStatisticsField()).isPresent() && StringUtils.isNotEmpty(it.getStatisticsField().getName()))
				.filter(it -> it.getCounter() > 0 && it.getStatisticsField().getName().equals(param))
				.map(Statistics::getCounter)
				.findFirst();
	}

	/**
	 * Utility method for calculation of start interval date
	 *
	 * @param interval Back interval
	 * @return Now minus interval
	 */
	private static LocalDateTime getStartIntervalDate(InfoInterval interval) {
		return LocalDateTime.now(Clock.systemUTC()).minusMonths(interval.getCount());
	}

	private static final Predicate<ActivityAction> ACTIVITIES_PROJECT_FILTER = it -> it == UPDATE_DEFECT || it == DELETE_DEFECT
			|| it == LINK_ISSUE || it == LINK_ISSUE_AA || it == UNLINK_ISSUE || it == UPDATE_ITEM;

	private Map<String, List<ChartObject>> getActivities(Long projectId, InfoInterval infoInterval) {
		String value = Arrays.stream(ActivityAction.values())
				.filter(not(ACTIVITIES_PROJECT_FILTER))
				.map(ActivityAction::getValue)
				.collect(joining(","));
		int limit = 150;
		Filter filter = new Filter(Activity.class, Sets.newHashSet(
				new FilterCondition(IN, false, value, CRITERIA_ACTION),
				new FilterCondition(EQUALS, false, String.valueOf(projectId), CRITERIA_PROJECT_ID),
				new FilterCondition(
						GREATER_THAN_OR_EQUALS,
						false,
						String.valueOf(Timestamp.valueOf(getStartIntervalDate(infoInterval)).getTime()),
						CRITERIA_CREATION_DATE
				)
		));
		//TODO find with sorting and limit
/*		List<Activity> activities = activityRepository.findByFilterWithSortingAndLimit(
				filter,
				Sort.by(Sort.Direction.DESC, "creation_date"),
				limit
		);*/
		List<Activity> activities = activityRepository.findByFilter(filter);

		List<ChartObject> chartObjects = activities.stream().map(it -> {
			ChartObject chartObject = new ChartObject();
			chartObject.setId(String.valueOf(it.getId()));
			HashMap<String, String> values = new HashMap<>();
			values.put("actionType", it.getAction());
			values.put("lastModified", String.valueOf(Timestamp.valueOf(it.getCreatedAt()).getTime()));
			values.put("objectType", it.getActivityEntityType().name());
			values.put("projectRef", String.valueOf(it.getProjectId()));
			values.put("userRef", String.valueOf(it.getUserId()));
			if (it.getObjectId() != null) {
				values.put("loggedObjectRef", String.valueOf(it.getObjectId()));
			}
			if (it.getDetails().getObjectName() != null) {
				values.put("name", it.getDetails().getObjectName());
			}
			it.getDetails().getHistory().forEach(val -> {
				values.put(val.getField() + "$oldValue", val.getOldValue());
				values.put(val.getField() + "$newValue", val.getNewValue());
			});
			chartObject.setValues(values);
			return chartObject;
		}).collect(toList());
		return Collections.singletonMap("result", chartObjects);
	}

}
