package com.epam.ta.reportportal.model.dashboard;

import com.epam.ta.reportportal.model.widget.WidgetConfigResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardConfigResource {

  @JsonProperty(value = "widgets")
  private List<WidgetConfigResource> widgets;

}
