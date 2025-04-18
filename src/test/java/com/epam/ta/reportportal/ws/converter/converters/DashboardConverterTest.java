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

package com.epam.ta.reportportal.ws.converter.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.model.activity.DashboardActivityResource;
import com.epam.ta.reportportal.model.dashboard.DashboardResource;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class DashboardConverterTest {

  @Test
  void toResourceNullTest() {
    assertThrows(NullPointerException.class, () -> DashboardConverter.TO_RESOURCE.apply(null));
  }

  @Test
  void toActivityResourceNullTest() {
    assertThrows(
        NullPointerException.class, () -> DashboardConverter.TO_ACTIVITY_RESOURCE.apply(null));
  }

  @Test
  void toActivityResource() {
    final Dashboard dashboard = getDashboard();
    final DashboardActivityResource activityResource =
        DashboardConverter.TO_ACTIVITY_RESOURCE.apply(dashboard);

    assertEquals(activityResource.getId(), dashboard.getId());
    assertEquals(activityResource.getName(), dashboard.getName());
    assertEquals(activityResource.getDescription(), dashboard.getDescription());
    assertEquals(activityResource.getProjectId(), dashboard.getProject().getId());
  }

  @Test
  void toResource() {
    final Dashboard dashboard = getDashboard();
    final DashboardResource resource = DashboardConverter.TO_RESOURCE.apply(dashboard);

    assertEquals(resource.getDashboardId(), dashboard.getId());
    assertEquals(resource.getName(), dashboard.getName());
    assertEquals(resource.getDescription(), dashboard.getDescription());
    assertEquals(resource.getOwner(), dashboard.getOwner());
    assertEquals(resource.getWidgets().size(), dashboard.getWidgets().size());
  }

  private static Dashboard getDashboard() {
    Dashboard dashboard = new Dashboard();
    dashboard.setId(1L);
    dashboard.setName("name");
    dashboard.setDescription("description");
    dashboard.setCreationDate(Instant.now());
    dashboard.setOwner("owner");
    final Project project = new Project();
    project.setId(2L);
    dashboard.setProject(project);
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
