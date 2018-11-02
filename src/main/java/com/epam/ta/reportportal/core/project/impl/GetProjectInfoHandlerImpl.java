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
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.project.GetProjectInfoHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectInfo;
import com.epam.ta.reportportal.entity.project.email.ProjectInfoWidget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @author Pavel Bortnik
 */
@Service
public class GetProjectInfoHandlerImpl implements GetProjectInfoHandler {

	private DecimalFormat formatter = new DecimalFormat("###.##");
	private static final Double WEEKS_IN_MONTH = 4.4;

	private final ProjectRepository projectRepository;

	private final LaunchRepository launchRepository;

	private final ProjectInfoWidgetDataConverter dataConverter;

	@Autowired
	public GetProjectInfoHandlerImpl(ProjectRepository projectRepository, LaunchRepository launchRepository,
			ProjectInfoWidgetDataConverter dataConverter) {
		this.projectRepository = projectRepository;
		this.launchRepository = launchRepository;
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
	public Map<String, String> getProjectInfoWidgetContent(ReportPortalUser.ProjectDetails projectDetails, String interval,
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

		Map<String, List<ChartObject>> result = Maps.newHashMap();

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
			//			case ACTIVITIES:
			//				result = getActivities(projectId, infoInterval);
			//				break;
			//			case LAST_LAUNCH:
			//				result = getLastLaunchStatistics(projectId);
			//				break;
			default:
				// empty result
				result = Collections.emptyMap();
		}

		return null;
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
}
