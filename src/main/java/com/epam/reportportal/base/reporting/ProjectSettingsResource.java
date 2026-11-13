package com.epam.reportportal.base.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectSettingsResource {

  @JsonProperty(value = "project", required = true)
  private Long projectId;

  @JsonProperty(value = "subTypes", required = true)
  private Map<String, List<IssueSubTypeResource>> subTypes;

}
