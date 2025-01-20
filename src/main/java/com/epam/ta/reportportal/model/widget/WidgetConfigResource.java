package com.epam.ta.reportportal.model.widget;

import com.epam.ta.reportportal.model.dashboard.DashboardResource.WidgetObjectModel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WidgetConfigResource {

  private WidgetObjectModel widgetObject;
  private WidgetResource widgetResource;

}
