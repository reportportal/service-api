/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.ReportPortalUser;
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
import java.util.Optional;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_NAME;
import static com.epam.ta.reportportal.commons.querygen.constant.ItemAttributeConstant.CRITERIA_ITEM_ATTRIBUTE_VALUE;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static java.util.stream.Collectors.toList;

@Service
class DemoDashboardsService {

	private static final String DASHBOARD_NAME = "DEMO DASHBOARD";
	private static final String FILTER_NAME = "DEMO_FILTER";
	private static final String START_TIME_SORTING = "startTime";
	private static final boolean SHARED = true;

	private final UserFilterRepository userFilterRepository;

	private final DashboardRepository dashboardRepository;

	private final DashboardWidgetRepository dashboardWidgetRepository;

	private final WidgetRepository widgetRepository;

	private final ProjectRepository projectRepository;

	private final ShareableObjectsHandler aclHandler;

	private final ObjectMapper objectMapper;

	private Resource resource;

	@Autowired
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

	@Value("classpath:demo/demo_widgets.json")
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	@Transactional
	public Optional<Dashboard> generate(ReportPortalUser user, Long projectId) {
		Project project = projectRepository.findById(projectId).orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));

		if (dashboardRepository.existsByNameAndOwnerAndProjectId(DASHBOARD_NAME, user.getUsername(), projectId)) {
			return Optional.empty();
		}

		UserFilter filter = createDemoFilter(user, project);
		List<Widget> widgets = createWidgets(user, projectId, filter);
		return Optional.of(createDemoDashboard(widgets, user, project, DASHBOARD_NAME));
	}

	private List<Widget> createWidgets(ReportPortalUser user, Long projectId, UserFilter filter) {
		try {
			TypeReference<List<WidgetRQ>> type = new TypeReference<>() {
			};

			List<Widget> widgets = objectMapper.readValue(resource.getURL(), type).stream().map(it -> {
				final WidgetBuilder widgetBuilder = new WidgetBuilder().addWidgetRq(it).addProject(projectId).addOwner(user.getUsername());
				final WidgetType widgetType = WidgetType.findByName(it.getWidgetType())
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_CREATE_WIDGET,
								"Widget type '" + it.getWidgetType() + "' does not exists"
						));
				if (!WidgetType.FLAKY_TEST_CASES.equals(widgetType) || !WidgetType.TOP_TEST_CASES.equals(widgetType)) {
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

	private UserFilter createDemoFilter(ReportPortalUser user, Project project) {
		List<UserFilter> existedFilterList = userFilterRepository.getPermitted(ProjectFilter.of(Filter.builder()
				.withTarget(UserFilter.class)
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.EQUALS)
						.withSearchCriteria(CRITERIA_NAME)
						.withValue(FILTER_NAME)
						.build())
				.build(), project.getId()), Pageable.unpaged(), user.getUsername()).getContent();

		if (!existedFilterList.isEmpty()) {
			return existedFilterList.get(0);
		}

		UserFilter userFilter = new UserFilter();
		userFilter.setName(FILTER_NAME);
		userFilter.setTargetClass(ObjectType.Launch);
		userFilter.setProject(project);
		userFilter.setFilterCondition(Sets.newHashSet(FilterCondition.builder()
				.withSearchCriteria(CRITERIA_ITEM_ATTRIBUTE_VALUE)
				.withCondition(Condition.HAS)
				.withValue("demo")
				.build()));

		FilterSort filterSort = new FilterSort();
		filterSort.setDirection(Sort.Direction.DESC);
		filterSort.setField(START_TIME_SORTING);
		userFilter.setFilterSorts(Sets.newHashSet(filterSort));

		userFilter.setOwner(user.getUsername());
		userFilter.setShared(SHARED);

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

		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(0), 0, 0, 6, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(1), 6, 0, 6, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(2), 0, 5, 7, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(3), 7, 5, 5, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(4), 0, 10, 5, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(5), 5, 10, 7, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(6), 0, 15, 6, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(7), 6, 15, 6, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(8), 0, 20, 12, 4));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(9), 0, 24, 7, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(10), 7, 24, 5, 5));
		dashboard.addWidget(createDashboardWidget(user.getUsername(), dashboard, widgets.get(11), 0, 29, 12, 4));

		aclHandler.initAcl(dashboard, user.getUsername(), project.getId(), SHARED);
		return dashboard;
	}

	private DashboardWidget createDashboardWidget(String owner, Dashboard dashboard, Widget widget, int posX, int posY, int width,
			int height) {
		DashboardWidget dashboardWidget = new DashboardWidget();
		dashboardWidget.setId(new DashboardWidgetId(dashboard.getId(), widget.getId()));

		dashboardWidget.setDashboard(dashboard);
		dashboardWidget.setWidget(widget);
		dashboardWidget.setWidgetName(widget.getName());
		dashboardWidget.setWidgetType(widget.getWidgetType());
		dashboardWidget.setCreatedOn(true);
		dashboardWidget.setWidgetOwner(owner);
		dashboardWidget.setHeight(height);
		dashboardWidget.setWidth(width);
		dashboardWidget.setPositionX(posX);
		dashboardWidget.setPositionY(posY);

		dashboardWidgetRepository.save(dashboardWidget);
		return dashboardWidget;
	}
}