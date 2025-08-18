package com.epam.ta.reportportal.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base response DTO for manual scenario operations.
 * Contains common information for all types of manual scenarios.
 * This class is extended by concrete implementations for different scenario types.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "manualScenarioType",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TmsStepsManualScenarioRS.class, name = "STEPS"),
    @JsonSubTypes.Type(value = TmsTextManualScenarioRS.class, name = "TEXT")
})
@Valid
public abstract class TmsManualScenarioRS {

  protected Long id;

  @Min(value = 1, message = "Execution estimation time must be positive")
  protected Integer executionEstimationTime;

  protected String linkToRequirements;

  protected TmsManualScenarioPreconditionsRS preconditions;

  @Valid
  protected Set<TmsAttributeRS> tags;

  /**
   * Type of manual scenario (STEPS or TEXT).
   * Required for proper JSON deserialization.
   */
  @NotNull(message = "Manual scenario type must be specified")
  protected TmsManualScenarioType manualScenarioType;
}
