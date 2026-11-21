package com.epam.reportportal.base.core.tms.dto;

import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import com.epam.reportportal.base.reporting.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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

  private TmsManualLaunchTestPlanRQ testPlan;

  private List<Long> testCaseIds;

  @Size(max = 100)
  @Schema(description = "Folders whose whole subtree of test cases is added")
  private List<Long> folderIds;

  private List<ItemAttributesRQ> attributes;
}
