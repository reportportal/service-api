/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.demodata;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserFilterRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.filter.FilterSort;
import com.epam.ta.reportportal.entity.filter.ObjectType;
import com.epam.ta.reportportal.entity.filter.UserFilter;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.querygen.constant.LaunchCriteriaConstant.CRITERIA_LAUNCH_TAG;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.model.ErrorType.PROJECT_NOT_FOUND;
import static com.epam.ta.reportportal.ws.model.ErrorType.RESOURCE_ALREADY_EXISTS;
import static java.util.stream.Collectors.toList;

@Service
class DemoDashboardsService {
	static final String D_NAME = "DEMO DASHBOARD";
	static final String F_NAME = "DEMO_FILTER";
	private final UserFilterRepository userFilterRepository;
	private final DashboardRepository dashboardRepository;
	private final WidgetRepository widgetRepository;
	private final ProjectRepository projectRepository;
	private final ObjectMapper objectMapper;
	private Resource resource;

	@Autowired
	DemoDashboardsService(UserFilterRepository userFilterRepository, DashboardRepository dashboardRepository,
			WidgetRepository widgetRepository, ProjectRepository projectRepository, ObjectMapper objectMapper) {
		this.userFilterRepository = userFilterRepository;
		this.dashboardRepository = dashboardRepository;
		this.widgetRepository = widgetRepository;
		this.projectRepository = projectRepository;
		this.objectMapper = objectMapper;
	}

	@Value("classpath:demo/demo_widgets.json")
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	Dashboard generate(DemoDataRq rq, ReportPortalUser user, Long projectId) {
		UserFilter filter = createDemoFilter(F_NAME, user, projectId);
		List<Widget> widgets = createWidgets(user, projectId, filter);
		return createDemoDashboard(widgets, user, projectId, D_NAME);
	}

	List<Widget> createWidgets(ReportPortalUser user, Long projectId, UserFilter filter) {
		try {
			TypeReference<List<Widget>> type = new TypeReference<List<Widget>>() {
			};

			/*
			List<Widget> existingWidgets = widgetRepository.findByProjectAndUser(projectDetails, user);
			If exists throw exception
			*/

			Project project = projectRepository.findById(projectId)
					.orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));

			List<Widget> widgets = ((List<Widget>) objectMapper.readValue(resource.getURL(), type)).stream().map(it -> {
				it.setProject(project);
				it.setFilters(Sets.newHashSet(filter));
				//				it.setWidgetType();
				String name = it.getName();
				//				checkUniqueName(name, existingWidgets);
				it.setName(name);
				return it;
			}).collect(toList());
			widgetRepository.saveAll(widgets);
			return widgets;
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load demo_widgets.json. " + e.getMessage(), e);
		}
	}

	UserFilter createDemoFilter(String filterName, ReportPortalUser user, Long projectId) {
		List<UserFilter> existedFilterList = userFilterRepository.findByFilter(Filter.builder()
				.withTarget(UserFilter.class)
				.withCondition(FilterCondition.builder()
						.withCondition(Condition.EQUALS)
						.withSearchCriteria("name")
						.withValue(filterName)
						.build())
				.build());

		expect(existedFilterList.size(), Predicate.isEqual(0)).verify(RESOURCE_ALREADY_EXISTS, filterName);

		Project project = projectRepository.findById(projectId).orElseThrow(() -> new ReportPortalException(PROJECT_NOT_FOUND, projectId));

		UserFilter userFilter = new UserFilter();
		userFilter.setName(filterName);
		userFilter.setTargetClass(ObjectType.Launch);
		userFilter.setProject(project);
		userFilter.setFilterCondition(Sets.newHashSet(new FilterCondition(Condition.HAS, false, "demo", CRITERIA_LAUNCH_TAG)));

		FilterSort filterSort = new FilterSort();
		filterSort.setDirection(Sort.Direction.DESC);
		filterSort.setField("start_time");
		userFilter.setFilterSorts(Sets.newHashSet(filterSort));

		return userFilterRepository.save(userFilter);
	}

	Dashboard createDemoDashboard(List<Widget> widgets, ReportPortalUser user, Long projectId, String name) {
		/*
		Dashboard existing = dashboardRepository.findOneByUserProject(user, projectDetails, name);
		expect(existing, isNull()).verify(RESOURCE_ALREADY_EXISTS, name);
		*/

		Dashboard dashboard = new Dashboard();
		dashboard.setName(name);

		widgets.stream().map(widget -> {
			DashboardWidget dashboardWidget = new DashboardWidget();

			Random rand = new Random();
			int maxHeight = 6;
			int minHeight = 4;
			int maxWidth = 12;
			int minWidth = 4;
			int maxXpos = 7;
			int maxYpos = 18;

			dashboardWidget.setDashboard(dashboard);
			dashboardWidget.setWidget(widget);
			dashboardWidget.setWidgetName(widget.getName());
			dashboardWidget.setHeight(rand.nextInt(maxHeight - minHeight + 1) + minHeight);
			dashboardWidget.setWidth(rand.nextInt(maxWidth - minWidth + 1) - minWidth);
			dashboardWidget.setPositionX(rand.nextInt(maxXpos));
			dashboardWidget.setPositionY(rand.nextInt(maxYpos));

			return dashboardWidget;
		}).forEach(dashboard::addWidget);
		dashboard.setProjectId(projectId);

		dashboard.setCreationDate(LocalDateTime.now());
		return dashboardRepository.save(dashboard);
	}

	/*
	private static Acl acl(String user, String project) {
		Acl acl = new Acl();
		acl.setOwnerUserId(user);
		AclEntry aclEntry = new AclEntry();
		aclEntry.setPermissions(singleton(READ));
		aclEntry.setProjectId(project);
		acl.setEntries(singleton(aclEntry));
		return acl;
	}
	*/
}