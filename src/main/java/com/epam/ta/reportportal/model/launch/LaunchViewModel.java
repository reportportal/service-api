package com.epam.ta.reportportal.model.launch;

import com.epam.ta.reportportal.entity.enums.LaunchTypeEnum;
import com.epam.ta.reportportal.ws.reporting.LaunchResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaunchViewModel extends LaunchResource {

  @JsonProperty("launchType")
  private LaunchTypeEnum launchType;

}
