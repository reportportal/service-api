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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LaunchTagRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.widget.content.LaunchesStatisticsContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Predicates.not;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.querygen.Condition.EQUALS;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static java.util.Collections.singletonMap;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.GetLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLaunchHandler /*extends StatisticBasedContentLoader*/ implements com.epam.ta.reportportal.core.launch.GetLaunchHandler {

	private final LaunchRepository launchRepository;
	private final LaunchTagRepository launchTagRepository;
	private final ProjectRepository projectRepository;
	private final WidgetContentRepository widgetContentRepository;

	public GetLaunchHandler(LaunchRepository launchRepository, LaunchTagRepository launchTagRepository, ProjectRepository projectRepository,
			WidgetContentRepository widgetContentRepository) {
		this.launchRepository = launchRepository;
		this.launchTagRepository = launchTagRepository;
		this.projectRepository = projectRepository;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public LaunchResource getLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, String username) {
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
		validate(launch, projectDetails, username);
		return TO_RESOURCE.apply(launch);
	}

	public LaunchResource getLaunchByProjectName(String projectName, Pageable pageable, Filter filter, String username) {

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND,
						"Project with name: " + projectName + " not found"
				));

		filter.withCondition(new FilterCondition(EQUALS, false, String.valueOf(project.getId()), PROJECT_ID));
		Page<Launch> launches = launchRepository.findByFilter(filter, pageable);
		expect(launches, notNull()).verify(LAUNCH_NOT_FOUND);
		return LaunchConverter.TO_RESOURCE.apply(launches.iterator().next());
	}

	public Iterable<LaunchResource> getProjectLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable,
			String userName) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, projectDetails.getProjectId()));
		Page<Launch> launches = launchRepository.findByFilter(ProjectFilter.of(filter, project.getId()), pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	public com.epam.ta.reportportal.ws.model.Page<LaunchResource> getLatestLaunches(ReportPortalUser.ProjectDetails projectDetails,
			Filter filter, Pageable pageable) {
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, projectDetails.getProjectId()));
		Page<Launch> launches = launchRepository.findByFilter(ProjectFilter.of(filter, project.getId()), pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	@Override
	public List<String> getTags(ReportPortalUser.ProjectDetails projectDetails, String value) {
		return launchTagRepository.getTags(projectDetails.getProjectId(), value);
	}

	@Override
	public List<String> getLaunchNames(ReportPortalUser.ProjectDetails projectDetails, String value) {
		expect(value.length() > 2, it -> Objects.equals(it, true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the launch name string '{}' is less than 3 symbols", value)
		);
		return launchRepository.getLaunchNames(projectDetails.getProjectId(), value, LaunchModeEnum.DEBUG);
	}

	@Override
	public List<String> getOwners(ReportPortalUser.ProjectDetails projectDetails, String value, String mode) {
		expect(value.length() > 2, Predicates.equalTo(true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);
		return launchRepository.getOwnerNames(projectDetails.getProjectId(), value, mode);
	}

	@Override
	public Map<String, List<LaunchesStatisticsContent>> getLaunchesComparisonInfo(ReportPortalUser.ProjectDetails projectDetails,
			Long[] ids) {

		List<String> contentFields = Lists.newArrayList(DEFECTS_AUTOMATION_BUG_TOTAL,
				DEFECTS_NO_DEFECT_TOTAL,
				DEFECTS_PRODUCT_BUG_TOTAL,
				DEFECTS_SYSTEM_ISSUE_TOTAL,
				DEFECTS_TO_INVESTIGATE_TOTAL,
				EXECUTIONS_FAILED,
				EXECUTIONS_PASSED,
				EXECUTIONS_SKIPPED
		);

		Filter filter = Filter.builder()
				.withTarget(Launch.class)
				.withCondition(new FilterCondition(Condition.IN,
						false,
						Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(",")),
						ID
				))
				.withCondition(new FilterCondition(EQUALS, false, String.valueOf(projectDetails.getProjectId()), PROJECT_ID))
				.build();

		List<LaunchesStatisticsContent> result = widgetContentRepository.launchesComparisonStatistics(filter,
				contentFields,
				Sort.unsorted(),
				ids.length
		);

		return singletonMap(RESULT, result);

	}

	@Override
	public Map<String, String> getStatuses(ReportPortalUser.ProjectDetails projectDetails, Long[] ids) {
		return launchRepository.getStatuses(projectDetails.getProjectId(), ids);
	}

	/**
	 * Validate user credentials and launch affiliation to the project
	 *
	 * @param launch         {@link Launch}
	 * @param projectDetails {@link com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails}
	 * @param username       User name
	 */
	private void validate(Launch launch, ReportPortalUser.ProjectDetails projectDetails, String username) {
		expect(launch.getProjectId(), Predicates.equalTo(projectDetails.getProjectId())).verify(ACCESS_DENIED);
		if (launch.getMode() == LaunchModeEnum.DEBUG) {
			Project project = projectRepository.findById(launch.getProjectId())
					.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND,
							"Project with id = " + launch.getProjectId() + " not found"
					));
			Project.UserConfig userConfig = ProjectUtils.findUserConfigByLogin(project, username);

			expect(userConfig, notNull()).verify(ErrorType.ACCESS_DENIED);
			expect(userConfig.getProjectRole(), not(Predicates.equalTo(ProjectRole.CUSTOMER))).verify(ACCESS_DENIED);
		}
	}

}