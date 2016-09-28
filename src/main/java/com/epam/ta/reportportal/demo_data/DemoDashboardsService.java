package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.database.entity.sharing.AclPermissions.READ;
import static com.epam.ta.reportportal.database.search.Condition.HAS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.filter.SelectionOptions;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.sharing.Acl;
import com.epam.ta.reportportal.database.entity.sharing.AclEntry;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
class DemoDashboardsService {
	private UserFilterRepository userFilterRepository;
	private DashboardRepository dashboardRepository;
	private WidgetRepository widgetRepository;
	private ObjectMapper objectMapper;
	@Value("classpath:demo/demo_widgets.json")
	private Resource resource;

	@Autowired
	DemoDashboardsService(UserFilterRepository userFilterRepository, DashboardRepository dashboardRepository,
			WidgetRepository widgetRepository, ObjectMapper objectMapper) {
		this.userFilterRepository = userFilterRepository;
		this.dashboardRepository = dashboardRepository;
		this.widgetRepository = widgetRepository;
		this.objectMapper = objectMapper;
	}

	Dashboard createDemoDashboard(DemoDataRq rq, String user, String project) {
		Dashboard dashboard = dashboardRepository.findOneByUserProject(user, project, rq.getDashboardName());
		if (dashboard != null) {
			throw new ReportPortalException(
					"Dashboard with name " + rq.getDashboardName() + " already exists. You couldn't create the duplicate.");
		}
		String filterId = createDemoFilter(rq.getFilterName(), user, project);
		Acl acl = acl(user, project);
		try {
			TypeReference<List<Widget>> type = new TypeReference<List<Widget>>() {
			};
			List<Widget> widgets = ((List<Widget>) objectMapper.readValue(resource.getURL(), type)).stream().map(it -> {
				it.setProjectName(project);
				it.setApplyingFilterId(filterId);
				it.setAcl(acl);
				return it;
			}).collect(toList());
			widgetRepository.save(widgets);
			return createDemoDashboard(widgets, user, project, rq.getDashboardName());

		} catch (IOException e) {
			throw new ReportPortalException("Unable to load demo_widgets.json");
		}
	}

	private String createDemoFilter(String filterName, String user, String project) {
		UserFilter existingFilter = userFilterRepository.findOneByName(user, filterName, project);
		if (existingFilter != null) {
			throw new ReportPortalException("User filter with name " + filterName + " already exists.  You couldn't create the duplicate.");
		}
		UserFilter userFilter = new UserFilter();
		userFilter.setName(filterName);
		userFilter.setFilter(new Filter(Launch.class, HAS, false, "demo", "tags"));
		SelectionOptions selectionOptions = new SelectionOptions();
		selectionOptions.setSortingColumnName("start_time");
		selectionOptions.setIsAsc(false);
		selectionOptions.setQuantity(50);
		selectionOptions.setPageNumber(1);
		userFilter.setSelectionOptions(selectionOptions);
		userFilter.setProjectName(project);
		userFilter.setIsLink(false);
		userFilter.setAcl(acl(user, project));
		return userFilterRepository.save(userFilter).getId();
	}

	private Dashboard createDemoDashboard(List<Widget> widgets, String user, String project, String name) {
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
