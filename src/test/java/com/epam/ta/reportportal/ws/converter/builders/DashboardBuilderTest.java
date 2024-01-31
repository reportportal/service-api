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

package com.epam.ta.reportportal.ws.converter.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.model.Position;
import com.epam.ta.reportportal.model.Size;
import com.epam.ta.reportportal.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.model.dashboard.UpdateDashboardRQ;
import java.util.Collections;
import org.junit.jupiter.api.Test;

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

    final Dashboard dashboard =
        new DashboardBuilder().addDashboardRq(createDashboardRQ).addOwner(owner)
            .addProject(projectId).get();

    assertEquals(name, dashboard.getName());
    assertEquals(description, dashboard.getDescription());
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
    updateDashboardRQ.setWidgets(Collections.singletonList(
        new DashboardResource.WidgetObjectModel("kek", 1L, new Size(10, 20),
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

    final Dashboard dashboard =
        new DashboardBuilder(existDashboard).addUpdateRq(updateDashboardRQ).get();

    assertEquals(name, dashboard.getName());
    assertEquals(description, dashboard.getDescription());
  }
}