package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import com.epam.reportportal.base.reporting.Mode;
import com.epam.reportportal.base.reporting.databind.MultiFormatDateDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmsManualLaunchRQ {

  private String name;

  private String uuid;

  private String startTime;

  @JsonProperty("mode")
  private Mode mode;

  private String description;

  private TmsManualLaunchTestPlanRQ testPlan;

  private List<Long> testCaseIds;

  private List<ItemAttributesRQ> attributes;
}
