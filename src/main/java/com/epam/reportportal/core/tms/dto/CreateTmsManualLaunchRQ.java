package com.epam.reportportal.core.tms.dto;

import com.epam.reportportal.reporting.ItemAttributesRQ;
import com.epam.reportportal.reporting.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Valid
public class CreateTmsManualLaunchRQ {

  private String name;

  private String uuid;

  private String startTime;

  @JsonProperty("mode")
  private Mode mode;

  private String description;

  @NotNull
  private Long testPlanId;

  private List<Long> testCaseIds;

  private List<ItemAttributesRQ> attributes;
}
