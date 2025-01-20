package com.epam.ta.reportportal.core.widget;

import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.model.dashboard.DashboardResource.WidgetObjectModel;
import com.epam.ta.reportportal.model.widget.WidgetConfigResource;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WidgetConfigurationService {

  private final WidgetRepository widgetRepository;

  public List<WidgetConfigResource> getWidgetsConfiguration(Dashboard dashboard,
      ProjectDetails projectDetails) {
    return dashboard.getDashboardWidgets().stream().map(WidgetConverter.TO_OBJECT_MODEL)
        .map(widget -> getWidgetConfig(widget, projectDetails)).collect(Collectors.toList());
  }

  private WidgetConfigResource getWidgetConfig(WidgetObjectModel widgetObject,
      ProjectDetails projectDetails) {
    var widget = widgetRepository.findByIdAndProjectId(widgetObject.getWidgetId(),
        projectDetails.getProjectId()).orElseThrow(
        () -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_PROJECT,
            widgetObject.getWidgetId(),
            projectDetails.getProjectName()
        ));
    return WidgetConfigResource.builder().widgetObject(widgetObject)
        .widgetResource(WidgetConverter.TO_WIDGET_RESOURCE.apply(widget)).build();
  }

}
