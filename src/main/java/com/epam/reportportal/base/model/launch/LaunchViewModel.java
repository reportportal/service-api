package com.epam.reportportal.base.model.launch;

import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.reporting.LaunchResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaunchViewModel extends LaunchResource {

  @JsonProperty("launchType")
  private LaunchTypeEnum launchType;

}
