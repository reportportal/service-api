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

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.commons.querygen.ProjectFilter;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.entity.widget.WidgetType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.WidgetBuilder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_VALUE;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.RESOURCE_ALREADY_EXISTS;
import static java.util.stream.Collectors.toList;

@Service
class DemoDashboardsService {

	private static final String DASHBOARD_NAME = "DEMO DASHBOARD";
	private static final String FILTER_NAME = "DEMO_FILTER";
	private static final String START_TIME_SORTING = "startTime";
	private static final boolean SHARED = true;
	private static int WIDGET_MAX_HEIGHT = 6;
	private static int WIDGET_MIN_HEIGHT = 4;
	private static int WIDGET_MAX_WIDTH = 12;
	private static int WIDGET_MIN_WIDTH = 4;
	private static int WIDGET_MAX_X_POS = 7;
	private static int WIDGET_MAX_Y_POS = 18;

	private final UserFilterRepository userFilterRepository;

	private final DashboardRepository dashboardRepository;

	private final DashboardWidgetRepository dashboardWidgetRepository;

	private final WidgetRepository widgetRepository;

	private final ProjectRepository projectRepository;

	private final ShareableObjectsHandler aclHandler;

	private final ObjectMapper objectMapper;

	private Resource resource;

	public DemoDashboardsService(UserFilterRepository userFilterRepository, DashboardRepository dashboardRepository,
			DashboardWidgetRepository dashboardWidgetRepository, WidgetRepository widgetRepository, ProjectRepository projectRepository,
			ShareableObjectsHandler aclHandler, ObjectMapper objectMapper) {
		this.userFilterRepository = userFilterRepository;
		this.dashboardRepository = dashboardRepository;
		this.dashboardWidgetRepository = dashboardWidgetRepository;
		this.widgetRepository = widgetRepository;
		this.projectRepository = projectRepository;
		this.aclHandler = aclHandler;
		this.objectMapper = objectMapper;
	}

	@Autowired

	@Value("classpath:demo/demo_widgets.json")
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Transactional
	public Dashboard generate(ReportPortalUser user, Long projectId) {
		Project project = projectRepository.findById(projectId).orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));

		UserFilter filter = createDemoFilter(FILTER_NAME, user, project);
		List<Widget> widgets = createWidgets(user, projectId, filter);
		return createDemoDashboard(widgets, user, project, DASHBOARD_NAME);
	}

	private List<Widget> createWidgets(ReportPortalUser user, Long projectId, UserFilter filter) {
		try {
			TypeReference<List<WidgetRQ>> type = new TypeReference<List<WidgetRQ>>() {
			};

			List<Widget> widgets = ((List<WidgetRQ>) objectMapper.readValue(resource.getURL(), type)).stream().map(it -> {
				final WidgetBuilder widgetBuilder = new WidgetBuilder().addWidgetRq(it).addProject(projectId).addOwner(user.getUsername());
				final WidgetType widgetType = WidgetType.findByName(it.getWidgetType())
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
								"Widget type '" + it.getWidgetType() + "' does not exists"
						));
				if (!WidgetType.FLAKY_TEST_CASES.equals(widgetType) || !WidgetType.PASSING_RATE_PER_LAUNCH.equals(widgetType)
						|| !WidgetType.TOP_TEST_CASES.equals(widgetType) || !WidgetType.ACTIVITY.equals(widgetType)) {
					widgetBuilder.addFilters(Sets.newHashSet(filter));
				}
				return widgetBuilder.get();
			}).collect(toList());
			widgetRepository.saveAll(widgets);
			widgets.forEach(it -> aclHandler.initAcl(it, user.getUsername(), projectId, it.isShared()));
			return widgets;
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load demo_widgets.json. " + e.getMessage(), e);
		}
	}

	private UserFilter createDemoFilter(String filterName, ReportPortalUser user, Project project) {
		List<UserFilter> existedFilterList = userFilterRepository.getPermitted(ProjectFilter.of(Filter.builder()
				.withTarget(UserFilter.class)
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.EQUALS)
						.withSearchCriteria(CRITERIA_NAME)
						.withValue(filterName)
						.build())
				.build(), project.getId()), Pageable.unpaged(), user.getUsername()).getContent();

		expect(existedFilterList.size(), Predicate.isEqual(0)).verify(RESOURCE_ALREADY_EXISTS, filterName);

		UserFilter userFilter = new UserFilter();
		userFilter.setName(filterName);
		userFilter.setTargetClass(ObjectType.Launch);
		userFilter.setProject(project);
		userFilter.setFilterCondition(Sets.newHashSet(new FilterCondition(Condition.HAS, false, "demo", CRITERIA_ITEM_ATTRIBUTE_VALUE)));

		FilterSort filterSort = new FilterSort();
		filterSort.setDirection(Sort.Direction.DESC);
		filterSort.setField(START_TIME_SORTING);
		userFilter.setFilterSorts(Sets.newHashSet(filterSort));

		userFilter.setOwner(user.getUsername());
		userFilter.setShared(true);

		userFilterRepository.save(userFilter);
		aclHandler.initAcl(userFilter, user.getUsername(), project.getId(), SHARED);

		return userFilter;
	}

	private Dashboard createDemoDashboard(List<Widget> widgets, ReportPortalUser user, Project project, String name) {
		Dashboard dashboard = new Dashboard();
		dashboard.setName(name);
		dashboard.setProject(project);
		dashboard.setCreationDate(LocalDateTime.now());
		dashboard.setOwner(user.getUsername());
		dashboard.setShared(SHARED);

		dashboardRepository.save(dashboard);

		widgets.stream().map(widget -> {
			DashboardWidget dashboardWidget = new DashboardWidget();
			dashboardWidget.setId(new DashboardWidgetId(dashboard.getId(), widget.getId()));

			dashboardWidget.setDashboard(dashboard);
			dashboardWidget.setWidget(widget);
			dashboardWidget.setWidgetName(widget.getName());
			dashboardWidget.setHeight(getRandomBetween(WIDGET_MIN_HEIGHT, WIDGET_MAX_HEIGHT));
			dashboardWidget.setWidth(getRandomBetween(WIDGET_MIN_WIDTH, WIDGET_MAX_WIDTH));
			dashboardWidget.setPositionX(getRandomBetween(0, WIDGET_MAX_X_POS));
			dashboardWidget.setPositionY(getRandomBetween(0, WIDGET_MAX_Y_POS));

			dashboardWidgetRepository.save(dashboardWidget);
			return dashboardWidget;
		}).forEach(dashboard::addWidget);

		aclHandler.initAcl(dashboard, user.getUsername(), project.getId(), SHARED);
		return dashboard;
	}

	private int getRandomBetween(int min, int max) {
		Random random = new Random();
		return random.ints(min, (max + 1)).findFirst().getAsInt();

	}
}