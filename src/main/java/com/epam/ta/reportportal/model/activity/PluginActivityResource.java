package com.epam.ta.reportportal.model.activity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PluginActivityResource {

  @JsonProperty(value = "id", required = true)
  private Long id;
  @JsonProperty(value = "name")
  private String name;

  @JsonProperty(value = "enabled")
  private boolean enabled;

  @JsonProperty(value = "version")
  private String version;

}
