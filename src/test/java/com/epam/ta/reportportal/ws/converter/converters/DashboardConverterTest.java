package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
public class DashboardConverterTest {

	@Test(expected = NullPointerException.class)
	public void toResourceNullTest() {
		DashboardConverter.TO_RESOURCE.apply(null);
	}

	@Test(expected = NullPointerException.class)
	public void toActivityResourceNullTest() {
		DashboardConverter.TO_ACTIVITY_RESOURCE.apply(null);
	}

	@Test(expected = NullPointerException.class)
	public void toSharedEntityNullTest() {
		DashboardConverter.TO_SHARED_ENTITY.apply(null);
	}

	@Test
	public void toSharedEntity() {
		final Dashboard dashboard = getDashboard();
		final SharedEntity sharedEntity = DashboardConverter.TO_SHARED_ENTITY.apply(dashboard);

		assertEquals(sharedEntity.getId(), String.valueOf(dashboard.getId()));
		assertEquals(sharedEntity.getName(), dashboard.getName());
		assertEquals(sharedEntity.getDescription(), dashboard.getDescription());
		assertEquals(sharedEntity.getOwner(), dashboard.getOwner());
	}

	@Test
	public void toActivityResource() {
		final Dashboard dashboard = getDashboard();
		final DashboardActivityResource activityResource = DashboardConverter.TO_ACTIVITY_RESOURCE.apply(dashboard);

		assertEquals(activityResource.getId(), dashboard.getId());
		assertEquals(activityResource.getName(), dashboard.getName());
		assertEquals(activityResource.getDescription(), dashboard.getDescription());
		assertEquals(activityResource.getProjectId(), dashboard.getProject().getId());
		assertEquals(activityResource.isShared(), dashboard.isShared());
	}

	@Test
	public void toResource() {
		final Dashboard dashboard = getDashboard();
		final DashboardResource resource = DashboardConverter.TO_RESOURCE.apply(dashboard);

		assertEquals(resource.getDashboardId(), dashboard.getId());
		assertEquals(resource.getName(), dashboard.getName());
		assertEquals(resource.getDescription(), dashboard.getDescription());
		assertEquals(resource.getOwner(), dashboard.getOwner());
		assertEquals(resource.isShare(), dashboard.isShared());
		assertEquals(resource.getWidgets().size(), dashboard.getDashboardWidgets().size());
	}

	private static Dashboard getDashboard() {
		Dashboard dashboard = new Dashboard();
		dashboard.setId(1L);
		dashboard.setName("name");
		dashboard.setDescription("description");
		dashboard.setCreationDate(LocalDateTime.now());
		dashboard.setOwner("owner");
		final Project project = new Project();
		project.setId(2L);
		dashboard.setProject(project);
		dashboard.setShared(true);
		final DashboardWidget dashboardWidget = new DashboardWidget();
		dashboardWidget.setId(new DashboardWidgetId(1L, 3L));
		dashboardWidget.setPositionY(2);
		dashboardWidget.setPositionX(3);
		dashboardWidget.setWidth(5);
		dashboardWidget.setHeight(6);
		dashboardWidget.setWidgetName("widgetName");
		dashboardWidget.setDashboard(dashboard);
		final Widget widget = new Widget();
		dashboardWidget.setWidget(widget);
		dashboard.addWidget(dashboardWidget);
		return dashboard;
	}
}