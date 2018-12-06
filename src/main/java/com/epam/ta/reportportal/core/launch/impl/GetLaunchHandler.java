/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.launch.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.dao.ItemAttributeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.WidgetContentRepository;
import com.epam.ta.reportportal.entity.enums.LaunchModeEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.entity.user.ProjectUser;
import com.epam.ta.reportportal.entity.widget.content.ChartStatisticsContent;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LaunchConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.Preconditions.HAS_ANY_MODE;
import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.querygen.Condition.EQUALS;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_PROJECT_ID;
import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_MODE;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.core.widget.content.constant.ContentLoaderConstants.RESULT;
import static com.epam.ta.reportportal.dao.constant.WidgetContentRepositoryConstants.*;
import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_RESOURCE;
import static com.epam.ta.reportportal.ws.model.ErrorType.*;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEBUG;
import static com.epam.ta.reportportal.ws.model.launch.Mode.DEFAULT;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.GetLaunchHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLaunchHandler /*extends StatisticBasedContentLoader*/ implements com.epam.ta.reportportal.core.launch.GetLaunchHandler {

	private final LaunchRepository launchRepository;
	private final ItemAttributeRepository itemAttributeRepository;
	private final ProjectRepository projectRepository;
	private final WidgetContentRepository widgetContentRepository;

	@Autowired
	public GetLaunchHandler(LaunchRepository launchRepository, ItemAttributeRepository itemAttributeRepository,
			ProjectRepository projectRepository, WidgetContentRepository widgetContentRepository) {
		this.launchRepository = launchRepository;
		this.itemAttributeRepository = itemAttributeRepository;
		this.projectRepository = projectRepository;
		this.widgetContentRepository = widgetContentRepository;
	}

	@Override
	public LaunchResource getLaunch(Long launchId, ReportPortalUser.ProjectDetails projectDetails, String username) {
		Launch launch = launchRepository.findById(launchId).orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
		validate(launch, projectDetails, username);
		return TO_RESOURCE.apply(launch);
	}

	@Override
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

	@Override
	public Iterable<LaunchResource> getProjectLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable,
			String userName) {
		validateModeConditions(filter);
		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		filter = addLaunchCommonCriteria(DEFAULT, filter, projectDetails.getProjectId());
		Page<Launch> launches = launchRepository.findByFilter(ProjectFilter.of(filter, project.getId()), pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	/*
	 * Changed logic for this method: It should return DEBUG launches for
	 * project users, for specified user or only owner
	 */
	@Override
	public Iterable<LaunchResource> getDebugLaunches(ReportPortalUser.ProjectDetails projectDetails, Filter filter, Pageable pageable) {
		filter = addLaunchCommonCriteria(DEBUG, filter, projectDetails.getProjectId());
		Page<Launch> launches = launchRepository.findByFilter(filter, pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	@Override
	public List<String> getAttributeKeys(ReportPortalUser.ProjectDetails projectDetails, String value) {
		return itemAttributeRepository.findLaunchAttributeKeys(projectDetails.getProjectId(), value, false);
	}

	@Override
	public List<String> getAttributeValues(ReportPortalUser.ProjectDetails projectDetails, String key, String value) {
		return itemAttributeRepository.findLaunchAttributeValues(projectDetails.getProjectId(), key, value, false);
	}

	@Override
	public com.epam.ta.reportportal.ws.model.Page<LaunchResource> getLatestLaunches(ReportPortalUser.ProjectDetails projectDetails,
			Filter filter, Pageable pageable) {

		validateModeConditions(filter);

		Project project = projectRepository.findById(projectDetails.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectDetails.getProjectId()));

		filter = addLaunchCommonCriteria(DEFAULT, filter, projectDetails.getProjectId());

		Page<Launch> launches = launchRepository.findAllLatestByFilter(ProjectFilter.of(filter, project.getId()), pageable);
		return PagedResourcesAssembler.pageConverter(LaunchConverter.TO_RESOURCE).apply(launches);
	}

	@Override
	public List<String> getLaunchNames(ReportPortalUser.ProjectDetails projectDetails, String value) {
		expect(value.length() > 2, it -> Objects.equals(it, true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the launch name string '{}' is less than 3 symbols", value)
		);
		return launchRepository.getLaunchNames(projectDetails.getProjectId(), value, LaunchModeEnum.DEFAULT.name());
	}

	@Override
	public List<String> getOwners(ReportPortalUser.ProjectDetails projectDetails, String value, String mode) {
		expect(value.length() > 2, Predicates.equalTo(true)).verify(INCORRECT_FILTER_PARAMETERS,
				formattedSupplier("Length of the filtering string '{}' is less than 3 symbols", value)
		);

		LaunchModeEnum launchMode = LaunchModeEnum.findByName(mode)
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_FILTER_PARAMETERS,
						formattedSupplier("Mode - {} doesn't exist.", mode)
				));

		return launchRepository.getOwnerNames(projectDetails.getProjectId(), value, launchMode.name());
	}

	@Override
	public Map<String, List<ChartStatisticsContent>> getLaunchesComparisonInfo(ReportPortalUser.ProjectDetails projectDetails, Long[] ids) {

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
						CRITERIA_ID
				)).withCondition(new FilterCondition(EQUALS, false, String.valueOf(projectDetails.getProjectId()), CRITERIA_PROJECT_ID))
				.build();

		List<ChartStatisticsContent> result = widgetContentRepository.launchesComparisonStatistics(filter,
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
			ProjectUser userConfig = ProjectUtils.findUserConfigByLogin(project, username);

			expect(userConfig, notNull()).verify(ErrorType.ACCESS_DENIED);
			expect(userConfig.getProjectRole(), not(Predicates.equalTo(ProjectRole.CUSTOMER))).verify(ACCESS_DENIED);
		}
	}

	/**
	 * Add to filter project and mode criteria
	 *
	 * @param filter Filter to update
	 * @return Updated filter
	 */
	private Filter addLaunchCommonCriteria(Mode mode, Filter filter, Long projectId) {

		List<FilterCondition> filterConditions = Lists.newArrayList(
				new FilterCondition(EQUALS, false, mode.toString(), CRITERIA_LAUNCH_MODE),
				new FilterCondition(EQUALS, false, String.valueOf(projectId), CRITERIA_PROJECT_ID)
		);

		return ofNullable(filter).orElseGet(() -> new Filter(Launch.class, Sets.newHashSet())).withConditions(filterConditions);
	}

	/**
	 * Validate if filter doesn't contain any "mode" related conditions.
	 *
	 * @param filter
	 */
	private void validateModeConditions(Filter filter) {
		expect(filter.getFilterConditions().stream().anyMatch(HAS_ANY_MODE), equalTo(false)).verify(INCORRECT_FILTER_PARAMETERS,
				"Filters for 'mode' aren't applicable for project's launches."
		);
	}

}