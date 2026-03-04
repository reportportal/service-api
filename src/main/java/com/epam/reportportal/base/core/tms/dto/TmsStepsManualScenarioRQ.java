package com.epam.reportportal.base.core.tms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Request DTO for step-based manual scenario operations.
 * Contains information about steps of the manual scenario.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsStepsManualScenarioRQ extends TmsManualScenarioRQ {

  private List<TmsStepRQ> steps;
}
