/*
 * Copyright 2025 EPAM Systems
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
package com.epam.ta.reportportal.core.widget;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.model.widget.WidgetConfigResource;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
@RequiredArgsConstructor
public class WidgetConfigurationService {

  private final WidgetRepository widgetRepository;

  public List<WidgetConfigResource> getWidgetsConfiguration(Dashboard dashboard,
      ProjectDetails projectDetails) {
    return dashboard.getDashboardWidgets().stream()
        .map(widget -> getWidgetConfig(widget, projectDetails)).collect(Collectors.toList());
  }

  private WidgetConfigResource getWidgetConfig(DashboardWidget dashboardWidget,
      ProjectDetails projectDetails) {
    var widgetId = dashboardWidget.getWidget().getId();
    var widget = widgetRepository.findByIdAndProjectId(widgetId,
        projectDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT, widgetId,
            projectDetails.getProjectName()
        ));
    return WidgetConfigResource.builder()
        .widgetObject(WidgetConverter.TO_OBJECT_MODEL.apply(dashboardWidget))
        .widgetResource(WidgetConverter.TO_WIDGET_RESOURCE.apply(widget)).build();
  }

}
