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
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.filter.SelectionOrder;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.widget.impl.WidgetUtils.checkUniqueName;
import static com.epam.ta.reportportal.database.entity.sharing.AclPermissions.READ;
import static com.epam.ta.reportportal.database.search.Condition.HAS;
import static com.epam.ta.reportportal.ws.model.ErrorType.RESOURCE_ALREADY_EXISTS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
class DemoDashboardsService {
	static final String D_NAME = "DEMO DASHBOARD";
	static final String F_NAME = "DEMO_FILTER";
	private final UserFilterRepository userFilterRepository;
	private final DashboardRepository dashboardRepository;
	private final WidgetRepository widgetRepository;
	private final ObjectMapper objectMapper;
	private Resource resource;

	@Autowired
	DemoDashboardsService(UserFilterRepository userFilterRepository, DashboardRepository dashboardRepository,
			WidgetRepository widgetRepository, ObjectMapper objectMapper) {
		this.userFilterRepository = userFilterRepository;
		this.dashboardRepository = dashboardRepository;
		this.widgetRepository = widgetRepository;
		this.objectMapper = objectMapper;
	}

	@Value("classpath:demo/demo_widgets.json")
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	Dashboard generate(DemoDataRq rq, String user, String project) {
		String dashboardName = D_NAME + "#" + rq.getPostfix();
		String filterName = F_NAME + "#" + rq.getPostfix();
		String filterId = createDemoFilter(filterName, user, project);
		List<Widget> widgets = createWidgets(rq.getPostfix(), user, project, filterId);
		return createDemoDashboard(widgets, user, project, dashboardName);
	}

	List<Widget> createWidgets(String postfix, String user, String project, String filterId) {
		try {
			TypeReference<List<Widget>> type = new TypeReference<List<Widget>>() {
			};
			List<Widget> existingWidgets = widgetRepository.findByProjectAndUser(project, user);
			List<Widget> widgets = ((List<Widget>) objectMapper.readValue(resource.getURL(), type)).stream().map(it -> {
				it.setProjectName(project);
				it.setApplyingFilterId(filterId);
				it.setAcl(acl(user, project));
				String name = it.getName() + "#" + postfix;
				checkUniqueName(name, existingWidgets);
				it.setName(name);
				return it;
			}).collect(toList());
			widgetRepository.save(widgets);
			return widgets;
		} catch (IOException e) {
			throw new ReportPortalException("Unable to load demo_widgets.json. " + e.getMessage(), e);
		}
	}

	String createDemoFilter(String filterName, String user, String project) {
		UserFilter existingFilter = userFilterRepository.findOneByName(user, filterName, project);
		expect(existingFilter, isNull()).verify(RESOURCE_ALREADY_EXISTS, filterName);
		UserFilter userFilter = new UserFilter();
		userFilter.setName(filterName);
		userFilter.setFilter(new Filter(Launch.class, HAS, false, "demo", "tags"));
		SelectionOptions selectionOptions = new SelectionOptions();
		SelectionOrder selectionOrder = new SelectionOrder();
		selectionOrder.setSortingColumnName("start_time");
		selectionOrder.setIsAsc(false);
		selectionOptions.setPageNumber(1);
		selectionOptions.setOrders(singletonList(selectionOrder));
		userFilter.setSelectionOptions(selectionOptions);
		userFilter.setProjectName(project);
		userFilter.setIsLink(false);
		userFilter.setAcl(acl(user, project));
		return userFilterRepository.save(userFilter).getId();
	}

	Dashboard createDemoDashboard(List<Widget> widgets, String user, String project, String name) {
		Dashboard existing = dashboardRepository.findOneByUserProject(user, project, name);
		expect(existing, isNull()).verify(RESOURCE_ALREADY_EXISTS, name);
		Dashboard dashboard = new Dashboard();
		dashboard.setName(name);
		ArrayList<Dashboard.WidgetObject> widgetObjects = new ArrayList<>();
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(0).getId(), asList(6, 5), asList(0, 0)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(1).getId(), asList(6, 5), asList(6, 0)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(2).getId(), asList(6, 4), asList(0, 5)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(3).getId(), asList(7, 4), asList(0, 9)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(4).getId(), asList(6, 4), asList(6, 5)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(5).getId(), asList(5, 4), asList(7, 9)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(6).getId(), asList(7, 5), asList(0, 13)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(7).getId(), asList(5, 5), asList(7, 13)));
		widgetObjects.add(new Dashboard.WidgetObject(widgets.get(8).getId(), asList(12, 5), asList(0, 18)));
		dashboard.setWidgets(widgetObjects);
		dashboard.setProjectName(project);
		dashboard.setCreationDate(new Date());
		dashboard.setAcl(acl(user, project));
		return dashboardRepository.save(dashboard);
	}

	private static Acl acl(String user, String project) {
		Acl acl = new Acl();
		acl.setOwnerUserId(user);
		AclEntry aclEntry = new AclEntry();
		aclEntry.setPermissions(singleton(READ));
		aclEntry.setProjectId(project);
		acl.setEntries(singleton(aclEntry));
		return acl;
	}
}
