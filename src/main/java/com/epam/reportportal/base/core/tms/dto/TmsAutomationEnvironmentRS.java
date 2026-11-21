package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The environments the autotests may run against, as set in the CI connection of the Test automation pipeline.
 * <p>
 * Example: {@code {"environments": ["beta5", "qa"], "default": "beta5"}}; the first environment is the default.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsAutomationEnvironmentRS {

  private List<String> environments;

  @JsonProperty("default")
  private String defaultEnvironment;
}
