package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.ws.model.Position;
import com.epam.ta.reportportal.ws.model.Size;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class DashboardBuilderTest {

	@Test
	void createDashboard() {
		final String name = "name";
		final String description = "description";
		final boolean share = true;
		final String owner = "owner";
		final Long projectId = 1L;

		CreateDashboardRQ createDashboardRQ = new CreateDashboardRQ();
		createDashboardRQ.setName(name);
		createDashboardRQ.setDescription(description);
		createDashboardRQ.setShare(share);

		final Dashboard dashboard = new DashboardBuilder().addDashboardRq(createDashboardRQ).addOwner(owner).addProject(projectId).get();

		assertEquals(name, dashboard.getName());
		assertEquals(description, dashboard.getDescription());
		assertEquals(share, dashboard.isShared());
		assertEquals(owner, dashboard.getOwner());
		assertEquals(projectId, dashboard.getProject().getId());
	}

	@Test
	void updateDashboard() {
		final String name = "name";
		final String description = "description";
		final boolean share = true;

		UpdateDashboardRQ updateDashboardRQ = new UpdateDashboardRQ();
		updateDashboardRQ.setName(name);
		updateDashboardRQ.setDescription(description);
		updateDashboardRQ.setShare(share);
		updateDashboardRQ.setWidgets(Collections.singletonList(new DashboardResource.WidgetObjectModel(1L,
				new Size(10, 20),
				new Position(30, 40)
		)));

		DashboardWidget dashboardWidget = new DashboardWidget();
		dashboardWidget.setHeight(5);
		dashboardWidget.setWidth(10);
		dashboardWidget.setPositionX(1);
		dashboardWidget.setPositionY(2);
		dashboardWidget.setId(new DashboardWidgetId(1L, 1L));

		final Dashboard existDashboard = new Dashboard();
		existDashboard.addWidget(dashboardWidget);

		final Dashboard dashboard = new DashboardBuilder(existDashboard).addUpdateRq(updateDashboardRQ).get();

		assertEquals(name, dashboard.getName());
		assertEquals(description, dashboard.getDescription());
		assertEquals(share, dashboard.isShared());
	}
}