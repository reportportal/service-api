package com.epam.reportportal.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class IssueSubTypeResource {

  @JsonProperty(value = "id")
  private Long id;

  @JsonProperty(value = "locator")
  private String locator;

  @JsonProperty(value = "typeRef")
  private String typeRef;

  @JsonProperty(value = "longName")
  private String longName;

  @JsonProperty(value = "shortName")
  private String shortName;

  @JsonProperty(value = "color")
  private String color;

}
